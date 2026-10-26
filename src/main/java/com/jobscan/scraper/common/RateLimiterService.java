package com.jobscan.scraper.common;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterService {
    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS_PER_MINUTE = 15;

    public void acquire(String source) {
        while (true) {

            long currentMinute = Instant.now().getEpochSecond() / 60;
            String key = "rate_limit:" + source.toLowerCase() + ":" + currentMinute;

            Long currentRequests = redisTemplate.opsForValue().increment(key);

            if (currentRequests != null && currentRequests == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(65));
            }

            if (currentRequests != null && currentRequests <= MAX_REQUESTS_PER_MINUTE) {
                return;
            }

            log.debug("Rate limit exceeded for {}. Virtual thread goes to sleep...", source);

            try {

                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Rate limiter interrupted", e);
            }
        }
    }
}
