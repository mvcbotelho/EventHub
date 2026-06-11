package com.marcus.eventhub.common.ratelimit;

public interface RateLimitBackend {

    boolean tryConsume(RateLimitBucket bucketType, String clientKey);
}
