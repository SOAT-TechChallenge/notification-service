package com.fiap.notification_service._webApi.dto;

import jakarta.validation.constraints.NotBlank;
public record SendEmailRequestDTO(
    
    @NotBlank(message = "O destinatário do email é obrigatório")
    String to,
    
    @NotBlank(message = "O assunto do email é obrigatório")
    String subject,

    @NotBlank(message = "O corpo do email é obrigatório")
    String body
) {

}
