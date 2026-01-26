package com.fiap.notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=1025",
    "spring.mail.username=test",
    "spring.mail.password=test",
    "spring.mail.properties.mail.smtp.auth=false",
    "spring.mail.properties.mail.smtp.starttls.enable=false",
    "app.mail.from=no-reply@test.com",

    "api.security.token.secret=segredo-de-teste-muito-seguro-123"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}