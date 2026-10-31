package com.jobscan.bot.handler;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.jobscan.bot.state.BotState;
import com.jobscan.bot.state.BotStateService;
import com.jobscan.bot.state.BotUserService;
import com.jobscan.config.BotUser;
import com.jobscan.config.UserProfileProperties;
import com.jobscan.vacancy.entity.VacancyStatus;
import com.jobscan.vacancy.service.VacancyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {
    private final BotStateService botStateService;
    private final UserProfileProperties userProfile;
    private final BotUserService botUserService;
    private final VacancyService vacancyService;

    public void handle(Message message, TelegramClient telegramClient) {
        String text = message.getText().trim();
        Long chatId = message.getChatId();

        BotState currentState = botStateService.getState(chatId);

        try {
            //
            if (currentState == BotState.WAITING_FOR_MATCH_THRESHOLD) {
                handleThresholdInput(chatId, text, telegramClient);
                return;
            }
            if (currentState == BotState.WAITING_FOR_URL) {
                handleUrlInput(chatId, text, telegramClient);
                return;
            }

            switch (text) {
                case "/start" -> sendResponse(chatId,
                        "👋 Hi! I am <b>Auto-Job Scan</b>.\n\nAvailable commands:\n/stats — job vacancy statistics\n/threshold — setting the scoring threshold",
                        telegramClient);
                case "/stats" -> sendStats(chatId, telegramClient);
                case "/threshold" -> {
                    botStateService.setState(chatId, BotState.WAITING_FOR_MATCH_THRESHOLD);
                    sendResponse(chatId, "Enter a new scoring threshold (a number from 0 to 100):", telegramClient);
                }
                case "/url" -> {
                    botStateService.setState(chatId, BotState.WAITING_FOR_URL);
                    sendResponse(chatId,
                            "Submit a new link with Djinni search filters (starts with https://djinni.co/jobs/):",
                            telegramClient);
                }
                default -> sendResponse(chatId, "Unknown command. Enter /start", telegramClient);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", text, e);
        }
    }

    private void handleUrlInput(Long chatId, String text, TelegramClient telegramClient) throws Exception {
        if (!text.startsWith("https://djinni.co/jobs/")) {
            sendResponse(chatId,
                    "⚠️ Invalid format. The link must start with https://djinni.co/jobs/. Try again, or enter /start to cancel.",
                    telegramClient);
            return;
        }

        botUserService.updateDjinniUrl(chatId, text);
        botStateService.clearState(chatId);
        sendResponse(chatId,
                "✅ The new search link has been successfully saved. It will be used the next time the parser is run.",
                telegramClient);
    }

    private void handleThresholdInput(Long chatId, String text, TelegramClient telegramClient) throws Exception {
        try {
            int threshold = Integer.parseInt(text);
            if (threshold < 0 || threshold > 100) {
                sendResponse(chatId, "⚠️ Please enter a valid number from 0 to 100.", telegramClient);
                return;
            }

            userProfile.setMatchThreshold(threshold);
            botUserService.updateThreshold(chatId, threshold);

            botStateService.clearState(chatId);
            sendResponse(chatId, "✅ The scoring threshold has been successfully changed to <b>" + threshold + "%</b>",
                    telegramClient);

        } catch (NumberFormatException e) {
            sendResponse(chatId,
                    "⚠️ This doesn't look like a number. Please enter a valid value, or type /start to cancel.",
                    telegramClient);
        }
    }

    private void sendStats(Long chatId, TelegramClient telegramClient) throws Exception {
        Map<VacancyStatus, Long> stats = vacancyService.getVacancyStats();
        BotUser user = botUserService.getOrCreateUser(chatId);

        long newCount = stats.getOrDefault(VacancyStatus.NEW, 0L);
        long appliedCount = stats.getOrDefault(VacancyStatus.APPLIED, 0L);
        long archivedCount = stats.getOrDefault(VacancyStatus.ARCHIVED, 0L);
        long rejectedCount = stats.getOrDefault(VacancyStatus.REJECTED, 0L);
        long total = newCount + appliedCount + archivedCount + rejectedCount;

        String text = String.format(
                "📊 <b>Service statistics:</b>\n\n" +
                        "Total vacancies: <b>%d</b>\n" +
                        "🆕 New: %d\n" +
                        "✅ Responses: %d\n" +
                        "📁 In the archive: %d\n" +
                        "🚫 Rejections: %d\n\n" +
                        "⚙️ Scoring threshold: <b>%d%%</b>",
                total, newCount, appliedCount, archivedCount, rejectedCount, user.getMatchThreshold(),
                user.getDjinniUrl());
        sendResponse(chatId, text, telegramClient);
    }

    private void sendResponse(Long chatId, String text, TelegramClient telegramClient) throws Exception {
        SendMessage response = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .disableWebPagePreview(true)
                .build();
        telegramClient.execute(response);
    }
}