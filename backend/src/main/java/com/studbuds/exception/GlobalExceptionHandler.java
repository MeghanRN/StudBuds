package com.studbuds.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Return a 400 Bad Request with the exception message.
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // ⚠️ returning a raw string may make it harder for the front end to handle errors.
        // ⚠️ consider creating a DTO for more structured error responses, and update exception handler?
    }
    
    // You can add more exception handlers if needed.
}
