package com.jobscan.bot.state;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobscan.config.BotUser;
import com.jobscan.config.UserProfileProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotUserService {
    private final BotUserRepository botUserRepository;
    private final UserProfileProperties userProfile;

    private static final String DEFAULT_DJINNI_URL = "https://djinni.co/jobs/?primary_keyword=Java&exp_level=2y&employment=remote";

    @Transactional
    public BotUser getOrCreateUser(Long chatId) {
        return botUserRepository.findById(chatId)
                .orElseGet(() -> botUserRepository
                        .save(new BotUser(chatId, userProfile.getMatchThreshold(), DEFAULT_DJINNI_URL)));
    }

    @Transactional
    public void updateDjinniUrl(Long chatId, String newUrl) {
        BotUser user = getOrCreateUser(chatId);
        user.setDjinniUrl(newUrl);
        botUserRepository.save(user);
    }

    @Transactional
    public void updateThreshold(Long chatId, Integer threshold) {
        BotUser user = getOrCreateUser(chatId);
        botUserRepository.save(new BotUser(chatId, threshold, user.getDjinniUrl()));
    }
}
