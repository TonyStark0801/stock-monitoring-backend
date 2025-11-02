package com.shubham.stockmonitoring.commons.config;

import com.shubham.stockmonitoring.commons.util.RedisService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {

    @Bean
    public RedisService redisService(RedisTemplate<String, String> redisTemplate) {
        return new RedisService(redisTemplate);
    }
}