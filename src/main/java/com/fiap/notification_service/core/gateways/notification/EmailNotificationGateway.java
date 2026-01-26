package com.fiap.notification_service.core.gateways.notification;


public interface EmailNotificationGateway {
    void sendEmail(String to, String subject, String body);
}
