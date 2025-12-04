package org.example.kpiservice.exception;

import org.springframework.http.HttpStatus;

public class RequestValidationException extends RuntimeException {
    private final HttpStatus status;

    public RequestValidationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public RequestValidationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static RequestValidationException from(String message) {
        return new RequestValidationException(message);
    }

    public static RequestValidationException from(HttpStatus status, String message) {
        return new RequestValidationException(status, message);
    }

    public static void throwFrom(HttpStatus status, String message) {
        throw new RequestValidationException(status, message);
    }
}
