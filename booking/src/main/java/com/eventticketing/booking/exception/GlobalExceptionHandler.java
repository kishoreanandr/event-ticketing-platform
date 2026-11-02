package com.eventticketing.booking.exception;


import com.eventticketing.booking.dto.BookingErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidBookingRequestException.class)
    public ResponseEntity<BookingErrorResponse> handleInvalidBookingRequest(
            InvalidBookingRequestException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<BookingErrorResponse> handleSeatNotAvailable(
            SeatNotAvailableException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BookingHoldExpiredException.class)
    public ResponseEntity<BookingErrorResponse> handleBookingHoldExpired(
            BookingHoldExpiredException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BookingErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<BookingErrorResponse> handleFeignException(
            FeignException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to communicate with another service",
                request
        );
    }

    @ExceptionHandler(NoFallbackAvailableException.class)
    public ResponseEntity<BookingErrorResponse> handlingCircuitBreakerException(
            NoFallbackAvailableException exception,
            HttpServletRequest request){
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "A dependent service is currently unavailable",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BookingErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        exception.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    private ResponseEntity<BookingErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        BookingErrorResponse response = new BookingErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}