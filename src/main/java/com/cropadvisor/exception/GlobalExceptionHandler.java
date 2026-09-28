package com.cropadvisor.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception, HttpServletRequest request) {
        HttpStatus status = determineBusinessStatus(exception.getMessage());
        return buildResponse(status, status.getReasonPhrase(), exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                "Malformed JSON request", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                "Invalid value for parameter: " + exception.getName(), request);
    }

    @ExceptionHandler({MissingPathVariableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                "Required request value is missing", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "HTTP method is not supported for this endpoint", request);
    }

    private HttpStatus determineBusinessStatus(String message) {
        String normalizedMessage = message == null ? "" : message.toLowerCase();
        if (normalizedMessage.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (normalizedMessage.contains("unauthorized")) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error,
                                                        String message, HttpServletRequest request) {
        return buildResponse(status, error, message, request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error,
                                                        String message, HttpServletRequest request,
                                                        Map<String, String> fieldErrors) {
        ErrorResponse response = new ErrorResponse(LocalDateTime.now(), status.value(), error,
                message, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(response);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(LocalDateTime timestamp, int status, String error,
                                String message, String path,
                                Map<String, String> fieldErrors) {
    }
}