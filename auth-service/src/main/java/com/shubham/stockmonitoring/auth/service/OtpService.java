package com.shubham.stockmonitoring.auth.service;

import com.shubham.stockmonitoring.auth.dto.request.ValidateOtpRequest;
import com.shubham.stockmonitoring.commons.util.ObjectUtil;
import com.shubham.stockmonitoring.commons.util.RedisService;
import com.shubham.stockmonitoring.commons.util.proxyUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

import static com.shubham.stockmonitoring.auth.Util.Constants.OTP_REDIS_EXPIRY;

@Service
@AllArgsConstructor
public class OtpService {

    private final SecureRandom secureRandom;
    private final proxyUtils proxyUtils;
    private final RedisService redisService;
    private final EmailService emailService;


    public String generateAndSendOtp(String email) {
        String otp = String.valueOf(100000 + secureRandom.nextInt(900000));
        String transactionId = UUID.randomUUID().toString();

        String key = proxyUtils.generateRedisKey("OTP", email, transactionId);
        redisService.set(key, otp, OTP_REDIS_EXPIRY);
//        emailService.sendOtpVerificationEmail(email, otp);
        return transactionId;
    }

    public String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    public void storeOtp(String email, String transactionId, String otp) {
        String key = proxyUtils.generateRedisKey("OTP", email, transactionId);
        redisService.set(key, otp, OTP_REDIS_EXPIRY);
    }

    public boolean validateOtp(ValidateOtpRequest request) {
        String key = proxyUtils.generateRedisKey("OTP", request.getEmail(), request.getTransactionId());
        String storedOtp = redisService.get(key);

        if (ObjectUtil.isNullOrEmpty(storedOtp)) return false;

        boolean isValid = storedOtp.equals(request.getOtp());
        if (isValid) {
            redisService.delete(key);
        }

        return isValid;
    }

}
