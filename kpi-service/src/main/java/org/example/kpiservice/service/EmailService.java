package org.example.kpiservice.service;

public interface EmailService {
    void sendHtmlMessage(String to, String subject, String htmlBody);

    void sendEmailWithAttachment(String to, String subject, String body, String filename, byte[] attachment);
}
