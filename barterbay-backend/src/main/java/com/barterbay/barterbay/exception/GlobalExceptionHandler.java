package com.barterbay.barterbay.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException exception, ServletWebRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception, ServletWebRequest request) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException exception, ServletWebRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException exception, ServletWebRequest request) {
        return buildError(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, ServletWebRequest request) {
        System.out.println("DEBUG - Unexpected exception in controller:");
        System.out.println("  Exception type: " + exception.getClass().getName());
        System.out.println("  Message: " + exception.getMessage());
        exception.printStackTrace();
        
        String errorMsg = "Unexpected server error: " + exception.getClass().getSimpleName() + " - " + exception.getMessage();
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg, request);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, ServletWebRequest request) {
        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequest().getRequestURI());

        return ResponseEntity.status(status).body(apiError);
    }
}
