package com.codenbugs.adsservice.exceptions;

import com.codenbugs.adsservice.controllers.pricing.AdPriceController;
import com.codenbugs.adsservice.security.SecurityConfig;
import com.codenbugs.adsservice.services.pricing.AdPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codenbugs.adsservice.dtos.exceptions.ErrorResponse;
import com.codenbugs.adsservice.dtos.exceptions.ValidationErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler handler;

    // ------------------------ Helpers ------------------------

    private void mockWebRequest() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/ad-prices");
    }

    // ------------------------ BusinessRuleException ------------------------

    @Test
    void whenBusinessRuleException_Returns400() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleIllegalStateException(
                new BusinessRuleException("Regla de negocio violada"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(400, response.getBody().getStatus()),
                () -> assertEquals("Bad Request", response.getBody().getError()),
                () -> assertEquals("Regla de negocio violada", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp()),
                () -> assertEquals("/api/v1/ad-prices", response.getBody().getPath())
        );
    }

    // ------------------------ NotFoundException ------------------------

    @Test
    void whenNotFoundException_Returns404() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(
                new NotFoundException("Recurso no encontrado"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(404, response.getBody().getStatus()),
                () -> assertEquals("Not Found", response.getBody().getError()),
                () -> assertEquals("Recurso no encontrado", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp()),
                () -> assertEquals("/api/v1/ad-prices", response.getBody().getPath())
        );
    }

    // ------------------------ DuplicatedEntityException ------------------------

    @Test
    void whenDuplicatedEntityException_Returns409() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleDuplicatedEntityException(
                new DuplicatedEntityException("Entidad duplicada"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(409, response.getBody().getStatus()),
                () -> assertEquals("Conflict", response.getBody().getError()),
                () -> assertEquals("Entidad duplicada", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp())
        );
    }

    // ------------------------ InvalidCredentialsException ------------------------

    @Test
    void whenInvalidCredentialsException_Returns401() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentialsException(
                new InvalidCredentialsException("Credenciales inválidas"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertEquals(401, response.getBody().getStatus()),
                () -> assertEquals("Unauthorized", response.getBody().getError()),
                () -> assertEquals("Credenciales inválidas", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp())
        );
    }

    // ------------------------ InvalidTokenException ------------------------

    @Test
    void whenInvalidTokenException_Returns401() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleInvalidTokenException(
                new InvalidTokenException("Token inválido"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertEquals(401, response.getBody().getStatus()),
                () -> assertEquals("Unauthorized", response.getBody().getError()),
                () -> assertEquals("Token inválido", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp())
        );
    }

    // ------------------------ ServiceException ------------------------

    @Test
    void whenServiceException_Returns500() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleServiceException(
                new ServiceException("Error de servicio"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals(500, response.getBody().getStatus()),
                () -> assertEquals("Internal Server Error", response.getBody().getError()),
                () -> assertEquals("Error de servicio", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp())
        );
    }

    // ------------------------ FileStorageException ------------------------

    @Test
    void whenFileStorageException_Returns500() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleFileStorageException(
                new FileStorageException("Error al almacenar archivo"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals(500, response.getBody().getStatus()),
                () -> assertEquals("Internal Server Error", response.getBody().getError()),
                () -> assertEquals("Error al almacenar archivo", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp())
        );
    }

    // ------------------------ AccessDeniedException ------------------------

    @Test
    void whenAccessDeniedException_Returns403() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(
                new AccessDeniedException("Acceso denegado"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()),
                () -> assertEquals(403, response.getBody().getStatus()),
                () -> assertEquals("Forbidden", response.getBody().getError()),
                () -> assertEquals("No tienes permisos para acceder a este recurso", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp())
        );
    }

    // ------------------------ Exception genérica ------------------------

    @Test
    void whenUnexpectedException_Returns500WithGenericMessage() {
        mockWebRequest();
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(
                new RuntimeException("Error inesperado"), webRequest
        );
        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals(500, response.getBody().getStatus()),
                () -> assertEquals("Internal Server Error", response.getBody().getError()),
                () -> assertEquals("Ha ocurrido un error inesperado en el servidor", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getTimestamp())
        );
    }
}