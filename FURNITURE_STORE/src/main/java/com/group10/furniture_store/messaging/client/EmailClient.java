package com.group10.furniture_store.messaging.client;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.group10.furniture_store.constant.AppConstant;
import com.group10.furniture_store.messaging.message.EmailRequest;
import com.group10.furniture_store.service.sendEmail.SendEmailService;

@Component
public class EmailClient {

    @Autowired
    private SendEmailService sendEmailService;

    @RabbitListener(queues = AppConstant.QUEUE)
    public void receiveEmailMessage(EmailRequest emailRequest) {
        sendEmailService.sendEmail(emailRequest.getToEmail(),
                emailRequest.getSubject(),
                emailRequest.getBody());
    }
}
