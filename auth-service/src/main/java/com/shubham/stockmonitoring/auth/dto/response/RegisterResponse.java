package com.shubham.stockmonitoring.auth.dto.response;

import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegisterResponse {
    String userId;
    String email;
    String name;
    boolean isEnabled;
    String message;
}
