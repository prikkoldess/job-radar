package com.jobscan.scraper.deduplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDeduplicationService {

    private static final String KEY_PREFIX = "job:seen:";
    private static final Duration TTL = Duration.ofDays(30);

    private final StringRedisTemplate stringRedisTemplate;

    public boolean isNew(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return false;
        }

        String key = buildKey(externalId);
        Boolean isAbsent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", TTL);

        boolean isNewJob = Boolean.TRUE.equals(isAbsent);
        if (!isNewJob) {
            log.debug("Duplicate detected for externalId: {}", externalId);
        }
        return isNewJob;
    }

    public boolean exists(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(externalId)));
    }

    private String buildKey(String externalId) {
        return KEY_PREFIX + externalId.trim();
    }
}
