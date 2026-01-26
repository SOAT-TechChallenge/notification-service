package com.fiap.notification_service.core.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage; // Se estiver usando Spring Boot 3+ (Jakarta EE)
// import javax.mail.internet.MimeMessage; // Se estiver usando Spring Boot 2 (Java EE)

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private JavaMailSender javaMailSender;

    private NotificationController notificationController;

    private final String MAIL_FROM = "teste@email.com";

    @BeforeEach
    void setUp() {
        // Como o método build é estático e instancia as coisas manualmente,
        // nós configuramos o controller real aqui passando o mock do JavaMailSender
        notificationController = NotificationController.build(javaMailSender, MAIL_FROM);
    }

    @Test
    @DisplayName("Deve executar o fluxo completo e tentar enviar o email via JavaMailSender")
    void shouldExecuteUseCaseAndCallJavaMailSender() {
        // Arrange
        String to = "usuario@destino.com";
        String subject = "Assunto Teste";
        String body = "Corpo do Email";

 
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        lenient().when(javaMailSender.createMimeMessage()).thenReturn(mimeMessageMock);

        // Act
        notificationController.sendEmail(to, subject, body);

        // Assert
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}