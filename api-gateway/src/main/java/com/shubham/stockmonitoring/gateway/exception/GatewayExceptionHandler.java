package com.shubham.stockmonitoring.gateway.exception;

import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

/**
 * Global exception handler for API Gateway (WebFlux-based)
 * This overrides the stock-commons handler which is servlet-based
 */
@Slf4j
@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGenericException(
            Exception ex, 
            ServerWebExchange exchange) {
        
        log.error("Unexpected error occurred in API Gateway", ex);

        BaseResponse<Object> response = BaseResponse.error(
                "GATEWAY_ERROR",
                "An error occurred while processing your request"
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<Object>> handleRuntimeException(
            RuntimeException ex, 
            ServerWebExchange exchange) {
        
        log.warn("Runtime exception in API Gateway: {}", ex.getMessage());

        BaseResponse<Object> response = BaseResponse.error(
                "GATEWAY_ERROR",
                ex.getMessage() != null ? ex.getMessage() : "An error occurred"
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

