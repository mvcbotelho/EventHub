package com.marcus.eventhub.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "eventhub.rate-limit.redis-enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryRateLimitBackend implements RateLimitBackend {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public InMemoryRateLimitBackend(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean tryConsume(RateLimitBucket bucketType, String clientKey) {
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
