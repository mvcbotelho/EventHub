package com.marcus.eventhub.common.ratelimit;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "eventhub.rate-limit.redis-enabled", havingValue = "true")
public class RedisRateLimitBackend implements RateLimitBackend {

    private static final String KEY_PREFIX = "eventhub:ratelimit:";

    private final RateLimitProperties properties;
    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitBackend(RateLimitProperties properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryConsume(RateLimitBucket bucketType, String clientKey) {
        int capacity = bucketType == RateLimitBucket.AUTH
                ? properties.getAuthCapacity()
                : properties.getRegistrationCapacity();
        int refillMinutes = bucketType == RateLimitBucket.AUTH
                ? properties.getAuthRefillPerMinutes()
                : properties.getRegistrationRefillPerMinutes();

        String key = KEY_PREFIX + bucketType.name() + ":" + clientKey;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(refillMinutes));
        }

        return count != null && count <= capacity;
    }
}
