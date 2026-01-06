package com.example.api_gateway.idempotency;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {
    private final ConcurrentHashMap<String, Integer> status = new ConcurrentHashMap<>();
    public boolean seen(String key) { return status.containsKey(key); }
    public void set(String key, int httpStatus) { status.put(key, httpStatus); }
    public Integer get(String key) { return status.get(key); }
}
