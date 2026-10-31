package com.jobscan.bot.state;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotStateService {
    private final StringRedisTemplate redisTemplate;
    private static final String STATE_PREFIX = "bot:state:";

    public void setState(Long chatId, BotState state) {
        redisTemplate.opsForValue().set(STATE_PREFIX + chatId, state.name(), Duration.ofHours(1));
    }

    public BotState getState(Long chatId) {
        String stateStr = redisTemplate.opsForValue().get(STATE_PREFIX + chatId);
        return stateStr != null ? BotState.valueOf(stateStr) : BotState.DEFAULT;
    }

    public void clearState(Long chatId) {
        redisTemplate.delete(STATE_PREFIX + chatId);
    }
}
