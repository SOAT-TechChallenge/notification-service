package com.fiap.notification_service.core.application.useCases.notification;

import com.fiap.notification_service.core.gateways.notification.EmailNotificationGateway;


public class SendEmailUseCase {


    private final EmailNotificationGateway emailNotificationGateway;

    public SendEmailUseCase(EmailNotificationGateway emailNotificationGateway) {
        this.emailNotificationGateway = emailNotificationGateway;
    }

    public void execute(String to, String subject, String body) {
        emailNotificationGateway.sendEmail(to, subject, body);
    }
}
