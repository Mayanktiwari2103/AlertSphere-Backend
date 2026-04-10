package com.alertsphere.backend.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String to, String message) {
        Message.creator(
                // 🚨 ADD "whatsapp:" HERE
                new PhoneNumber("whatsapp:" + to),
                new PhoneNumber("whatsapp:" + twilioNumber),
                message
        ).create();
        System.out.println("✅ WhatsApp Sent to: " + to);
    }
}