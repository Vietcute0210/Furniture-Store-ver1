package com.group10.furniture_store.service.sendEmail;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.var;

@Service
public class SendEmail {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("furniturestoreonline247@gmail.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        mailSender.send(message);
    }

    public void sendEmailWithHTML(String toEmail, String subject) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("furniturestoreonline247@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            try (var inputStream = Objects
                    .requireNonNull(SendEmail.class.getResourceAsStream("/mail/resetPass.html"))) {
                helper.setText(
                        new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), true

                );

            }
            mailSender.send(message);
        } catch (Exception ex) {
        }
    }
}
