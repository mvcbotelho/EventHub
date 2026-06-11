package com.marcus.eventhub.common.ratelimit;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RateLimitProperties properties;
    private final RateLimitBackend backend;

    public RateLimitService(RateLimitProperties properties, RateLimitBackend backend) {
        this.properties = properties;
        this.backend = backend;
    }

    public boolean tryConsume(RateLimitBucket bucketType, String clientKey) {
        if (!properties.isEnabled()) {
            return true;
        }
        return backend.tryConsume(bucketType, clientKey);
    }
}
