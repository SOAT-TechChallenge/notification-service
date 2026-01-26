package com.fiap.notification_service._webApi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import com.fiap.notification_service._webApi.dto.SendEmailRequestDTO;
import com.fiap.notification_service.core.controller.NotificationController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "Notification", description = "APIs relacionadas ao envio de notificações")
public class NotificationWebController {

    private final NotificationController notificationController;

    public NotificationWebController (JavaMailSender javaMailSender, @Value("${app.mail.from}") String mailFrom) {
        this.notificationController = NotificationController.build(javaMailSender, mailFrom);
    }

    @PostMapping("/send-email")
    @Operation(summary = "Send Email",
        description = "Envia um email para o destinatário informado")
    public ResponseEntity<Void> sendEmail(@RequestBody @Valid SendEmailRequestDTO dto) {
        this.notificationController.sendEmail(dto.to(), dto.subject(), dto.body());
        return ResponseEntity.ok().build();
    }
}
