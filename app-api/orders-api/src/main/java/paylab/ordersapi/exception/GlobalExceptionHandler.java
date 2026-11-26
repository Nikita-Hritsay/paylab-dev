package paylab.ordersapi.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import paylab.paymentprovider.exceptions.LiqPayException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> business(BusinessException ex) {
        return body(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "Malformed request body", null);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> badParameter(Exception ex) {
        return body(HttpStatus.BAD_REQUEST, "Missing or invalid request parameter", null);
    }

    @ExceptionHandler(LiqPayException.class)
    public ResponseEntity<Map<String, Object>> paymentProvider(LiqPayException ex) {
        log.error("Payment provider error", ex);
        return body(HttpStatus.BAD_GATEWAY, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, Object details) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", Instant.now());
        b.put("status", status.value());
        b.put("message", message);
        if (details != null) {
            b.put("errors", details);
        }
        return ResponseEntity.status(status).body(b);
    }
}
