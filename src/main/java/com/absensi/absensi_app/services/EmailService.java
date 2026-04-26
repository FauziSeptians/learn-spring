package com.absensi.absensi_app.services;

public interface EmailService {
    void sendNotification(String to, String subject, String body);
}
