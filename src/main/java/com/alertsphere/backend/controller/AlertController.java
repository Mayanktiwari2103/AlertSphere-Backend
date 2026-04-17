package com.alertsphere.backend.controller;
import com.alertsphere.backend.service.GeminiService;
import com.alertsphere.backend.service.TwilioService; // 🚨 Add this
import com.alertsphere.backend.repository.UserRepository; // 🚨 Add this
import java.util.concurrent.CompletableFuture; // 🚨 Add this
import com.alertsphere.backend.model.Alert;
import com.alertsphere.backend.repository.AlertRepository;
import com.alertsphere.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.alertsphere.backend.service.S3Service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.alertsphere.backend.model.User;

@CrossOrigin(origins = {"http://localhost:5173", "https://alert-sphere-tau.vercel.app"})
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private  GeminiService geminiService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<Alert> createAlert(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "alertType", defaultValue = "General") String alertType,
            @RequestParam("severity") String severity,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("locationName") String locationName,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        Alert alert = new Alert();
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setLatitude(latitude);
        alert.setLongitude(longitude);
        alert.setTimestamp(LocalDateTime.now());
        alert.setLocationName(locationName);

        // Initialize status
        alert.setUpvotes(0);
        alert.setDownvotes(0);
        alert.setVerified(false);

        // --- GEMINI AI VERIFICATION LOGIC ---
        // We check if the incident is real before saving.
        String aiVerification = geminiService.verifyIncident(description);
        if ("REAL".equalsIgnoreCase(aiVerification)) {
            alert.setVerified(true);
            System.out.println("🤖 Gemini: Incident verified as REAL.");
        } else {
            alert.setVerified(false);
            System.out.println("🤖 Gemini: Incident flagged as potentially FAKE.");
        }

        // AWS S3 Logic: Upload the image and save the URL
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image);
            alert.setImageUrl(imageUrl); // Ensure Alert.java has an 'imageUrl' field
        }

        Alert savedAlert = alertRepository.save(alert);

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/alerts", savedAlert);

       // Only send SMS if Gemini verified it as REAL to save Twilio credits/spam
        if (savedAlert.isVerified()) {
            CompletableFuture.runAsync(() -> {
                try {
                    List<User> users = userRepository.findAll();
                    String smsMessage = "🚨 AlertSphere: " + savedAlert.getAlertType() +
                            " reported at " + savedAlert.getLocationName() +
                            ". Title: " + savedAlert.getTitle();

                    for (User user : users) {
                        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                            twilioService.sendSms(user.getPhoneNumber(), smsMessage);
                        }
                    }
                } catch (Exception e) {
                    System.err.println(" Twilio Broadcast Error: " + e.getMessage());
                }
            });
        }

        return ResponseEntity.ok(savedAlert);
    }

    // New Vote Endpoint
    @PostMapping("/{id}/vote")
    public ResponseEntity<Alert> voteAlert(@PathVariable Long id, @RequestParam String type) {
        return alertRepository.findById(id).map(alert -> {
            if ("up".equalsIgnoreCase(type)) {
                alert.setUpvotes(alert.getUpvotes() + 1);
            } else if ("down".equalsIgnoreCase(type)) {
                alert.setDownvotes(alert.getDownvotes() + 1);
            }

            // Trust Logic: Auto-verify if community trust is high (e.g., 5 net upvotes)
            if (alert.getUpvotes() - alert.getDownvotes() >= 5) {
                alert.setVerified(true);
            }

            Alert updatedAlert = alertRepository.save(alert);

            // 🔥 Broadcast the updated vote counts to all users
            messagingTemplate.convertAndSend("/topic/alerts", updatedAlert);

            return ResponseEntity.ok(updatedAlert);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 1. SECURE VERIFY
    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyAlert(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        // Security Check
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access Denied: Only Admins can verify incidents.");
        }

        return alertRepository.findById(id).map(alert -> {
            alert.setVerified(true);
            Alert updated = alertRepository.save(alert);
            // Broadcast the update so everyone's map updates instantly!
            messagingTemplate.convertAndSend("/topic/alerts", updated);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 2. SECURE DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        // Security Check
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access Denied: Only Admins can delete incidents.");
        }

        alertRepository.deleteById(id);
        // Optional: Broadcast a 'delete' message via WebSocket if you want it to vanish from others' maps
        return ResponseEntity.ok("Incident deleted successfully.");
    }
}