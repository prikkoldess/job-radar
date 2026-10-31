package com.jobscan.bot;

import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.jobscan.bot.handler.CallbackQueryHandler;
import com.jobscan.bot.handler.CommandHandler;
import com.jobscan.config.TelegramProperties;
import com.jobscan.vacancy.entity.Vacancy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramBotService implements SpringLongPollingBot, LongPollingUpdateConsumer {
    private final TelegramClient telegramClient;
    private final TelegramProperties telegramProperties;
    private final CallbackQueryHandler callbackQueryHandler;
    private final CommandHandler commandHandler;

    public TelegramBotService(TelegramProperties telegramProperties, CallbackQueryHandler callbackQueryHandler,
            CommandHandler commandHandler) {
        this.telegramProperties = telegramProperties;
        this.callbackQueryHandler = callbackQueryHandler;
        this.commandHandler = commandHandler;
        this.telegramClient = new OkHttpTelegramClient(telegramProperties.getToken());
    }

    @Override
    public String getBotToken() {
        return telegramProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasCallbackQuery()) {
                callbackQueryHandler.handle(update.getCallbackQuery(), telegramClient);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                commandHandler.handle(update.getMessage(), telegramClient);
            }
        }
    }

    public void sendVacancyAlert(Vacancy vacancy) {
        String chatId = telegramProperties.getOwnerChatId();

        String missingSkills = vacancy.getSkillsBreakdown().missingSkills().isEmpty()
                ? "No (100% match)"
                : String.join(", ", vacancy.getSkillsBreakdown().missingSkills());

        String text = String.format(
                "🔥 <b>New vacancy (Match: %d%%)</b>\n\n" +
                        "<b>%s</b>\n" +
                        "🏢 %s\n" +
                        "💰 %s\n\n" +
                        "<i>Lacking skills:</i> %s\n\n" +
                        "<a href=\"%s\">🔗 Open a job vacancy</a>",
                vacancy.getMatchScore(),
                vacancy.getTitle(),
                vacancy.getCompany().getName(),
                vacancy.getSalaryFork().isEmpty() ? "Not specified" : vacancy.getSalaryFork(),
                missingSkills,
                vacancy.getUrl());

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(createAlertKeyboard(vacancy.getId()))
                .build();

        try {
            telegramClient.execute(message);
            log.info("Alert sent to owner for vacancy ID: {}", vacancy.getId());
        } catch (Exception e) {
            log.error("Failed to send alert for vacancy: {}", vacancy.getId(), e);
        }
    }

    private InlineKeyboardMarkup createAlertKeyboard(Long vacancyId) {
        InlineKeyboardButton btnApplied = InlineKeyboardButton.builder()
                .text("✅ Responded")
                .callbackData("APPLY_" + vacancyId)
                .build();

        InlineKeyboardButton btnArchive = InlineKeyboardButton.builder()
                .text("📁 To the archive")
                .callbackData("ARCHIVE_" + vacancyId)
                .build();

        InlineKeyboardButton btnHideCompany = InlineKeyboardButton.builder()
                .text("🚫 Hide company")
                .callbackData("HIDE_COMPANY_" + vacancyId)
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(btnApplied, btnArchive))
                .keyboardRow(new InlineKeyboardRow(btnHideCompany))
                .build();
    }
}