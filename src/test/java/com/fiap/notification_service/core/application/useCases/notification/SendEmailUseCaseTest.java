package com.fiap.notification_service.core.application.useCases.notification;

import com.fiap.notification_service.core.gateways.notification.EmailNotificationGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Habilita o uso das anotações @Mock e @InjectMocks
class SendEmailUseCaseTest {

    @Mock
    private EmailNotificationGateway emailNotificationGateway;

    @InjectMocks
    private SendEmailUseCase sendEmailUseCase;

    @Test
    @DisplayName("Deve chamar o gateway de email com os parâmetros corretos")
    void shouldCallGatewayWithCorrectParameters() {
        // Arrange
        String to = "usuario@teste.com";
        String subject = "Bem-vindo";
        String body = "Olá, seja bem-vindo ao sistema!";

        // Act
        sendEmailUseCase.execute(to, subject, body);

        // Assert
        verify(emailNotificationGateway, times(1)).sendEmail(to, subject, body);
    }
}