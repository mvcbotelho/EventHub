package com.marcus.eventhub.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public boolean tryConsume(RateLimitBucket bucketType, String clientKey) {
        if (!properties.isEnabled()) {
            return true;
        }

        String cacheKey = bucketType.name() + ":" + clientKey;
        Bucket bucket = cache.computeIfAbsent(cacheKey, ignored -> createBucket(bucketType));
        return bucket.tryConsume(1);
    }

    private Bucket createBucket(RateLimitBucket bucketType) {
        int capacity;
        int refillMinutes;

        if (bucketType == RateLimitBucket.AUTH) {
            capacity = properties.getAuthCapacity();
            refillMinutes = properties.getAuthRefillPerMinutes();
        } else {
            capacity = properties.getRegistrationCapacity();
            refillMinutes = properties.getRegistrationRefillPerMinutes();
        }

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(refillMinutes))
                .build();

        return Bucket.builder().addLimit(limit).build();
    }
}
