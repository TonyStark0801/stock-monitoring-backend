package com.shubham.stockmonitoring.auth.service;

import com.shubham.stockmonitoring.commons.util.RedisService;
import com.shubham.stockmonitoring.commons.util.proxyUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.shubham.stockmonitoring.auth.Util.Constants.OTP_REDIS_EXPIRY;

@Service
@AllArgsConstructor
public class OtpService {

    private final SecureRandom secureRandom;
    private final proxyUtils proxyUtils;
    private final RedisService redisService;
    private final EmailService emailService;


    public String generateAndSendOtp(String email) {
        String otp  = String.valueOf(100000 + secureRandom.nextInt(900000));
        String transactionId = UUID.randomUUID().toString();

        String key = proxyUtils.generateRedisKey("OTP", email, transactionId);
        redisService.set(key, otp, OTP_REDIS_EXPIRY);
        emailService.sendOtpVerificationEmail(email, otp);
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

    public boolean validateOtp(String email, String otp) {
        String key = "otp:" + email;
        String storedOtp = redisService.get(key);

        if (storedOtp == null) {
            return false; // OTP expired or doesn't exist
        }

        boolean isValid = storedOtp.equals(otp);
        if (isValid) {
            redisService.delete(key);
        }

        return isValid;
    }

    public void invalidateOtp(String email) {
        String key = "otp:" + email;
        redisService.delete(key);
    }
}
