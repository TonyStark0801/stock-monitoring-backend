package com.shubham.stockmonitoring.commons.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Set a key-value pair with no expiration
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set a key-value pair with expiration in seconds
     */
    public void set(String key, String value, long expirationInSeconds) {
        redisTemplate.opsForValue().set(key, value, expirationInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Get value by key
     */
    @Nullable
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Delete a key
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * Delete multiple keys
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * Check if key exists
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Set expiration on existing key (in seconds)
     */
    public Boolean expire(String key, long expirationInSeconds) {
        return redisTemplate.expire(key, expirationInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Get remaining time to live in seconds
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Increment a numeric value
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * Increment a numeric value by delta
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * Decrement a numeric value
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * Decrement a numeric value by delta
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * Find keys matching a pattern (use with caution in production)
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * Set if key does not exist (returns true if set was successful)
     */
    public Boolean setIfAbsent(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * Set if key does not exist with expiration in seconds
     */
    public Boolean setIfAbsent(String key, String value, long expirationInSeconds) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expirationInSeconds, TimeUnit.SECONDS);
    }
}