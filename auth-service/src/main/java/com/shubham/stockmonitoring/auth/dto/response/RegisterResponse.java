package com.shubham.stockmonitoring.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegisterResponse {
    String transactionId;
    String message;
}
