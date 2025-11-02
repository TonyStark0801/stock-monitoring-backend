package com.shubham.stockmonitoring.commons.exception;

import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(CustomException.class)
        public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex, HttpServletRequest request) {
            log.warn("Custom exception: {} - {}", ex.getErrorType(), ex.getCustomMessage());

            BaseResponse<Object> response = BaseResponse.error(
                    ex.getHttpStatus().name(),
                    ex.getCustomMessage() != null ? ex.getCustomMessage() : "An error occurred"
            );

            return new ResponseEntity<>(response, ex.getHttpStatus());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<BaseResponse<Object>> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
            log.warn("Authentication failed: {}", ex.getMessage());

            BaseResponse<Object> response = BaseResponse.error(
                    "AUTHENTICATION_FAILED",
                    "Invalid username or password"
            );

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<BaseResponse<Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
            log.warn("Authentication exception: {}", ex.getMessage());

            BaseResponse<Object> response = BaseResponse.error(
                    "AUTHENTICATION_FAILED",
                    "Authentication failed"
            );

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<BaseResponse<Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
            log.warn("Validation failed: {}", ex.getMessage());

            Map<String, String> errors = new HashMap<>();
            ex.getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });

            BaseResponse<Object> response = BaseResponse.error(
                    "VALIDATION_ERROR",
                    "Validation failed: " + errors.toString()
            );

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<BaseResponse<Object>> handleMissingParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
            log.warn("Missing required parameter: {}", ex.getMessage());

            BaseResponse<Object> response = BaseResponse.error(
                    "MISSING_PARAMETER",
                    "Required parameter '" + ex.getParameterName() + "' is missing"
            );

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<BaseResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
            log.warn("Type mismatch for parameter: {}", ex.getMessage());

            BaseResponse<Object> response = BaseResponse.error(
                    "INVALID_PARAMETER_TYPE",
                    "Invalid value for parameter '" + ex.getName() + "'. Expected " + ex.getRequiredType().getSimpleName()
            );

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<BaseResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
            // Only unexpected exceptions are logged as ERROR
            log.error("Unexpected error occurred", ex);

            BaseResponse<Object> response = BaseResponse.error(
                    "INTERNAL_ERROR",
                    "An unexpected error occurred"
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
