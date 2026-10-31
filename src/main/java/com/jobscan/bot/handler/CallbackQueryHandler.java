package com.jobscan.bot.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.jobscan.vacancy.entity.VacancyStatus;
import com.jobscan.vacancy.service.CompanyService;
import com.jobscan.vacancy.service.VacancyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private final VacancyService vacancyService;
    private final CompanyService companyService;

    public void handle(CallbackQuery callbackQuery, TelegramClient telegramClient) {
        String data = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String chatId = callbackQuery.getMessage().getChatId().toString();

        try {
            if (data.startsWith("APPLY_")) {
                Long vacancyId = Long.parseLong(data.replace("APPLY_", ""));
                vacancyService.updateStatus(vacancyId, VacancyStatus.APPLIED);
                removeKeyboard(telegramClient, chatId, messageId);
            } else if (data.startsWith("ARCHIVE_")) {
                Long vacancyId = Long.parseLong(data.replace("ARCHIVE_", ""));
                vacancyService.updateStatus(vacancyId, VacancyStatus.ARCHIVED);
                removeKeyboard(telegramClient, chatId, messageId);
            } else if (data.startsWith("HIDE_COMPANY_")) {
                Long vacancyId = Long.parseLong(data.replace("HIDE_COMPANY_", ""));
                Long companyId = vacancyService.getCompanyIdByVacancy(vacancyId);
                if (companyId != null) {
                    companyService.blockCompany(companyId);
                }
                removeKeyboard(telegramClient, chatId, messageId);
            }
        } catch (Exception e) {
            log.error("Failed to handle callback query: {}", data, e);
        }
    }

    private void removeKeyboard(TelegramClient telegramClient, String chatId, Integer messageId) throws Exception {
        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(null)
                .build();
        telegramClient.execute(editMarkup);
    }
}
