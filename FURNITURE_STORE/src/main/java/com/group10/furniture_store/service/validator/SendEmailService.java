package com.group10.furniture_store.service.validator;

import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.group10.furniture_store.constant.AppConstant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SendEmailService {
    public String getRandom() {
        Random random = new Random();
        int num = random.nextInt(999999);
        return String.format("%06d", num);
    }

    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(AppConstant.SYSTEM_EMAIL_SENDER);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
