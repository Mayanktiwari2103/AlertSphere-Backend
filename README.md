# 🚨 AlertSphere: AI-Powered Emergency Dispatch System


**AlertSphere** is a crowdsourced, real-time emergency reporting platform. It empowers citizens to report local hazards (accidents, extreme weather, power outages) and utilizes Generative AI to instantly verify the plausibility of reports, automatically dispatching WhatsApp alerts to the community.

---

## ✨ Key Features

* **🗺️ Interactive Geo-Mapping:** Integrated with Google Maps & Geocoding APIs. Users can drop pins on their current location or search for remote addresses to report incidents.
* **🤖 AI-Powered Verification:** Integrates **Google Gemini 2.5 Flash** to act as an automated logic filter. The AI analyzes incident descriptions in real-time to filter out spam, pranks, or fake news before verifying the alert.
* **📲 Instant WhatsApp Dispatch:** Once an incident is verified (by AI or an Admin), the system triggers a **Twilio Webhook** to instantly broadcast emergency WhatsApp alerts to registered users in the area.
* **⚡ Real-Time UI Sync:** Utilizes **WebSockets** to push live updates to the frontend. When a new incident is reported, the map updates instantly for all active users without requiring a page refresh.
* **☁️ Optimized Cloud Storage:** Incident images are uploaded directly to an **AWS S3 Bucket**, keeping the primary MySQL database lightweight and highly performant.
* **🔐 Role-Based Access & Auth:** Supports secure Google OAuth and manual registration. Includes Guest (view-only), User (report/vote), and Admin (manual verification/deletion) roles.

---

## 🛠️ The Tech Stack

### Frontend
* **Framework:** React.js
* **Mapping:** Google Maps JavaScript API
* **Deployment:** Vercel

### Backend
* **Framework:** Spring Boot (Java 17+)
* **Database:** MySQL
* **Real-time:** WebSockets (STOMP)
* **Deployment:** Render

### Cloud & Third-Party APIs
* **AI Engine:** Google AI Studio (Gemini 2.5 Flash)
* **Messaging:** Twilio API (WhatsApp Sandbox)
* **Storage:** Amazon Web Services (AWS S3)
* **Location:** Google Geocoding API

---

## 🏗️ System Architecture Flow

1. **Submission:** User submits a report via the React frontend (Category, Location, Description, Image).
2. **Storage:** The image is uploaded to AWS S3; the URL is returned and saved alongside the text data in MySQL.
3. **AI Analysis:** The backend constructs a prompt using the incident data and sends it to the Gemini 2.5 API via a custom Spring `RestClient`.
4. **Resolution:** * If Gemini returns `[REAL]`, the incident is auto-verified.
   * If Gemini returns `[FAKE]`, it remains pending for manual Admin review.
5. **Dispatch:** Upon verification, Twilio dispatches WhatsApp messages, and WebSockets broadcast the new incident marker to all connected React clients.

---

## 🚀 Getting Started (Local Development)

If you wish to run this project locally, you will need to configure several API keys.

### Prerequisites
* Java 17+
* Node.js & npm
* MySQL Server
* Accounts for: Google Cloud Console, Google AI Studio, Twilio, and AWS.

### Environment Variables
Create an `application.properties` (or `.env`) file in your backend resources with the following structure:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/alertsphere
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

# Google Gemini AI
gemini.api.key=YOUR_GEMINI_API_KEY

# Twilio (WhatsApp)
twilio.account.sid=YOUR_TWILIO_SID
twilio.auth.token=YOUR_TWILIO_TOKEN
twilio.phone.number=whatsapp:+14155238886

# AWS S3
aws.access.key=YOUR_AWS_ACCESS_KEY
aws.secret.key=YOUR_AWS_SECRET_KEY
aws.s3.bucket.name=YOUR_BUCKET_NAME
aws.region=YOUR_AWS_REGION
