package org.example.kpiservice.exception;

import org.example.kpiservice.dtos.ApiResponseEntity;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.error("Authentication error: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        logger.error("Bad credentials: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.UNAUTHORIZED, "Wrong username or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.error("Access denied: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.FORBIDDEN, "You don't have permission to access this resource");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleJwtException(JwtException ex, WebRequest request) {
        logger.error("JWT error: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseEntity<Void, Map<String, String>>>
    handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        logger.error("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            if (fieldError.getDefaultMessage() != null) {
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        }

        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseEntity<Void, Map<String, String>>>
    handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        logger.error("Constraint violation: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            fieldErrors.put(fieldName, errorMessage);
        }

        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        logger.error("JSON parse error: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, "Invalid request format provided");
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleBrandManagementApiException(ApiException ex, WebRequest request) {
        logger.warn("API error :: status: {}, message: {}", ex.getStatus(), ex.getMessage());
        return ApiResponseEntity.error(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.error("Illegal argument: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleDataAccessException(DataAccessException ex, WebRequest request) {
        logger.error("DB error: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        logger.error("DB unique key validation error: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, "A record with this unique key already exists.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime error: {}", ex.getMessage());
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseEntity<Void, Void>>
    handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred", ex);
        return ApiResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

}