package com.shubham.stockmonitoring.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String userId;
    private String name;
    private String email;
    private String role;
}
