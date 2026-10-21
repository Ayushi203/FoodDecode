package com.fooddecode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean mailConfigured;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${mail.username:CHANGE_ME}") String from) {
        this.mailSender = mailSender;
        this.from = from;
        this.mailConfigured = from != null && !from.isBlank() && !"CHANGE_ME".equals(from);
    }

    public void sendOtp(String to, String otp) {
        String body = "Your FoodDecode password-reset OTP is " + otp + ". It expires in 10 minutes.";
        if (!mailConfigured) {
            log.info("Mail not configured — OTP for {} is {}", to, otp);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("FoodDecode password reset OTP");
        message.setText(body);
        mailSender.send(message);
    }
}
