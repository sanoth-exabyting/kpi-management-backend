package org.example.kpiservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Schema(description = "Generic API Response")
public record ApiResponseEntity<D, E>(
        @Schema(description = "Response HTTP Status") String status,
        @Schema(description = "Response HTTP Status Message") String message,
        @Schema(description = "Response HTTP Success Data") D data,
        @Schema(description = "Response HTTP Error Data") E error) {

    public static <D> ResponseEntity<ApiResponseEntity<D, Void>> created(D data) {
        return success(HttpStatus.CREATED, "Success", data);
    }

    public static <D> ResponseEntity<ApiResponseEntity<D, Void>> ok(D data) {
        return success(HttpStatus.OK, "Success", data);
    }

    public static <D> ResponseEntity<ApiResponseEntity<D, Void>>
    success(HttpStatus status, String message, D data) {
        return create(status, message, data, null);
    }

    public static <E> ResponseEntity<ApiResponseEntity<Void, Void>>
    error(HttpStatus status, String message) {
        return create(status, message, null, null);
    }

    public static <E> ResponseEntity<ApiResponseEntity<Void, E>>
    error(HttpStatus status, String message, E error) {
        return create(status, message, null, error);
    }

    private static <D, E> ResponseEntity<ApiResponseEntity<D, E>>
    create(HttpStatus status, String message, D data, E error) {
        return new ResponseEntity<>(new ApiResponseEntity<>(status.getReasonPhrase(), message, data, error), status);
    }
}
