package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public void sendNotification(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(this.mailUsername);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("EMAIL_SENT | Ke: [{}] | Subjek: [{}]", to, subject);
        } catch (Exception e) {
            log.error("EMAIL_ERROR | Gagal mengirim email ke [{}]: {}", to, e.getMessage());
        }
    }
}

