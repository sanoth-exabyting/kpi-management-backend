package org.example.kpiservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    private ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    private ApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static ApiException badRequest(String message) {
        return create(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException notFound(String message) {
        return create(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException create(HttpStatus status, String message) {
        return new ApiException(status, message);
    }

    public static ApiException create(HttpStatus status, String message, Throwable cause) {
        return new ApiException(status, message, cause);
    }
}