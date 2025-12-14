package org.example.kpiservice.service;

public interface EmailService {
    void sendHtmlMessage(String to, String subject, String htmlBody);
}
