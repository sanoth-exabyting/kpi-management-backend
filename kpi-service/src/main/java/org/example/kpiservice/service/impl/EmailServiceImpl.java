package org.example.kpiservice.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.from_mail}")
    private String fromEmail;

    @Async
    @Override
    public void sendHtmlMessage(String to, String subject, String htmlBody) {
        try {
            // todo:need to remove this line in production
            to = "sanoth14@cse.pstu.ac.bd";
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            emailSender.send(message);
            log.info("HTML email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending HTML email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String filename, byte[] attachment) {
        try {
            // todo:need to remove this line in production
            to = "sanoth14@cse.pstu.ac.bd";

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            // Add attachment
            helper.addAttachment(filename, new ByteArrayResource(attachment));

            emailSender.send(message);
            log.info("Email with attachment sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email with attachment to {}: {}", to, e.getMessage());
        }
    }
}
