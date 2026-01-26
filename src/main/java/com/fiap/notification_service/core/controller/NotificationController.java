package com.fiap.notification_service.core.controller;

import org.springframework.mail.javamail.JavaMailSender;

import com.fiap.notification_service.core.application.useCases.notification.SendEmailUseCase;
import com.fiap.notification_service.core.gateways.notification.EmailNotificationGateway;
import com.fiap.notification_service.core.gateways.notification.EmailNotificationGatewayImpl;

public class NotificationController {


    private final EmailNotificationGateway emailNotificationGateway;

    private NotificationController(JavaMailSender javaMailSender, String mailFrom) {
        this.emailNotificationGateway = new EmailNotificationGatewayImpl(javaMailSender, mailFrom);
    }

    public static NotificationController build(JavaMailSender javaMailSender, String mailFrom) {
        return new NotificationController(javaMailSender, mailFrom);
    }


    public void sendEmail(String to, String subject, String body) {
        SendEmailUseCase useCase = new SendEmailUseCase(emailNotificationGateway);
        useCase.execute(to, subject, body);
    }
}
