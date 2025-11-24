package com.shubham.stockmonitoring.masterdata.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockRequest {
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    private String sector;
    private String exchange;
    
    @NotNull(message = "Current price is required")
    @Positive(message = "Current price must be positive")
    private BigDecimal currentPrice;
    
    private BigDecimal marketCap;
    private Long volume;
}
