package com.fiap.notification_service.core.gateways.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationGatewayImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailNotificationGatewayImpl emailNotificationGateway;

    private final String FROM_ADDRESS = "no-reply@fiap.com.br";

    @BeforeEach
    void setUp() {
        emailNotificationGateway = new EmailNotificationGatewayImpl(mailSender, FROM_ADDRESS);
    }

    @Test
    @DisplayName("Deve enviar email com sucesso")
    void shouldSendEmailSuccessfully() {
        // Arrange
        String to = "cliente@teste.com";
        String subject = "Atualização de Pedido";
        String body = "<h1>Seu pedido mudou de status</h1>";

        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageMock);

        // Act
        emailNotificationGateway.sendEmail(to, subject, body);

        // Assert
        verify(mailSender, times(1)).send(mimeMessageMock);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ocorrer MessagingException")
    void shouldThrowExceptionWhenMessagingExceptionOccurs() throws MessagingException {
        // Arrange
        String to = "cliente@teste.com";
        String subject = "Erro";
        String body = "Body";

        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageMock);

        doThrow(new MessagingException("Falha simulada"))
            .when(mimeMessageMock).setSubject(any(), any()); 

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailNotificationGateway.sendEmail(to, subject, body);
        });

        assertEquals("Erro ao enviar e-mail: Falha simulada", exception.getMessage());
        verify(mailSender, never()).send(mimeMessageMock);
    }

    @Test
    @DisplayName("Deve formatar o conteúdo HTML corretamente com ID e Status")
    void shouldBuildHtmlContentCorrectly() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String status = "APROVADO";

        // Act
        String resultHtml = emailNotificationGateway.buildHtmlContent(orderId, status);

        // Assert
        assertNotNull(resultHtml);
        assertTrue(resultHtml.contains(orderId.toString()));
        assertTrue(resultHtml.contains(status));
    }
}