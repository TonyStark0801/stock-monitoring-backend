package com.shubham.stockmonitoring.commons.util;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
public class ValidationUtils {

    /**
     * Check if String is null or empty
     */
    public boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if Object is null or empty
     * Handles: Collection, Map, Array, String
     */
    public boolean isNullOrEmpty(Object obj) {
        return switch (obj) {
            case null -> true;
            case String s -> s.trim().isEmpty();
            case Collection<?> collection -> collection.isEmpty();
            case Map<?, ?> map -> map.isEmpty();
            case Optional<?> optional -> optional.isEmpty();
            case Object[] array -> array.length == 0;
            default -> obj.getClass().isArray() &&
                       java.lang.reflect.Array.getLength(obj) == 0;
        };
    }
}
