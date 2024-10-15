package com.finance.transactionmanager.exceptions;

import com.finance.transactionmanager.exceptions.custom.BadRequestException;
import com.finance.transactionmanager.exceptions.custom.InternalServerErrorException;
import com.finance.transactionmanager.exceptions.custom.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler({
            Exception.class,
            RuntimeException.class,
            InternalServerErrorException.class
    })
    public ResponseEntity<StandardError> handleInternalServerError(Throwable e, HttpServletRequest request) {
        return getStandardErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e, request);
    }

    @ExceptionHandler({
            NotFoundException.class
    })
    public ResponseEntity<StandardError> handleNotFound(Throwable e, HttpServletRequest request) {
        return getStandardErrorResponseEntity(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler({
            BadRequestException.class,
            PropertyValueException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<StandardError> handleBadRequest(Throwable e, HttpServletRequest request) {
        return getStandardErrorResponseEntity(HttpStatus.BAD_REQUEST, e, request);
    }

    private ResponseEntity<StandardError> getStandardErrorResponseEntity(HttpStatus status,
                                                                         Throwable e,
                                                                         HttpServletRequest request) {
        logError(e);

        return ResponseEntity
                .status(status)
                .body(StandardError.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(e.getMessage())
                        .path(request.getRequestURL().toString())
                        .build());
    }

    private void logError(Throwable e) {
        log.error(e.getMessage(), e);
    }
}
