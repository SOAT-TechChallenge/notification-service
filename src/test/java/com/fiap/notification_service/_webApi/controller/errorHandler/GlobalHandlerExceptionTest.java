package com.fiap.notification_service._webApi.controller.errorHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalHandlerExceptionTest {

    @InjectMocks
    private GlobalHandlerException handler;

    @Mock
    private BindingResult bindingResult;

    private enum TestEnum { VALUE_A, VALUE_B }

    @Test
    void shouldHandleGenericExceptions() {
        RuntimeException ex = new RuntimeException("Generic error");

        ResponseEntity<ErrorResponse> response = handler.handleExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Generic error", response.getBody().getMessage());
    }

    @Test
    void shouldHandleSQLIntegrityConstraintViolationWithDuplicateEntry() {
        String msg = "Some preamble Duplicate entry 'test@email.com' for key 'users.email' some suffix";
        SQLIntegrityConstraintViolationException ex = new SQLIntegrityConstraintViolationException(msg);

        ResponseEntity<ErrorResponse> response = handler.handleSQLIntegrityConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Duplicate entry 'test@email.com'", response.getBody().getMessage());
    }

    @Test
    void shouldHandleSQLIntegrityConstraintViolationWithoutDuplicatePattern() {
        String msg = "Generic constraint violation";
        SQLIntegrityConstraintViolationException ex = new SQLIntegrityConstraintViolationException(msg);

        ResponseEntity<ErrorResponse> response = handler.handleSQLIntegrityConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(msg, response.getBody().getMessage());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<HashMap<String, Object>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        HashMap<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro(s) de validação", body.get("message"));
        
        List<HashMap<String, String>> errors = (List<HashMap<String, String>>) body.get("errors");
        assertEquals("field", errors.get(0).get("field"));
        assertEquals("must not be null", errors.get(0).get("message"));
    }

    @Test
    void shouldHandleInvalidEnumValueException() {
        InvalidFormatException invalidFormatEx = mock(InvalidFormatException.class);
        JsonMappingException.Reference pathRef = new JsonMappingException.Reference(null, "statusField");
        
        when(invalidFormatEx.getTargetType()).thenReturn((Class) TestEnum.class);
        when(invalidFormatEx.getPath()).thenReturn(List.of(pathRef));
        
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Error", invalidFormatEx);

        ResponseEntity<ErrorResponse> response = handler.handleInvalidEnumValueException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Valor inválido para o campo 'statusField'"));
        assertTrue(response.getBody().getMessage().contains("VALUE_A"));
    }

    @Test
    void shouldHandleGenericHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Generic parsing error");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidEnumValueException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Generic parsing error", response.getBody().getMessage());
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchExceptionForEnum() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        
        when(ex.getRequiredType()).thenReturn((Class) TestEnum.class);
        when(ex.getName()).thenReturn("type");
        when(ex.getValue()).thenReturn("INVALID");

        ResponseEntity<ErrorResponse> response = handler.handleEnumPathVariableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String msg = response.getBody().getMessage();
        assertTrue(msg.contains("Valor inválido 'INVALID' para o campo 'type'"));
        assertTrue(msg.contains("VALUE_A"));
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchExceptionForUUID() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        
        when(ex.getRequiredType()).thenReturn((Class) UUID.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getValue()).thenReturn("123-invalid");

        ResponseEntity<ErrorResponse> response = handler.handleEnumPathVariableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Deve ser um UUID válido"));
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchExceptionUnknownType() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        
        when(ex.getRequiredType()).thenReturn((Class) String.class);

        ResponseEntity<ErrorResponse> response = handler.handleEnumPathVariableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Parâmetro inválido.", response.getBody().getMessage());
    }
}