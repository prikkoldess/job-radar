package com.jobscan.queue.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobscan.bot.TelegramBotService;
import com.jobscan.config.RabbitMQConfig;
import com.jobscan.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramAlertConsumer {
    private final VacancyRepository vacancyRepository;
    private final TelegramBotService telegramBotService;

    @Transactional(readOnly = true)
    @RabbitListener(queues = RabbitMQConfig.ALERTS_QUEUE)
    public void consumeAlert(Long vacancyId) {
        log.info("Received alert event for vacancy ID: {}", vacancyId);

        vacancyRepository.findById(vacancyId).ifPresentOrElse(
                telegramBotService::sendVacancyAlert,
                () -> log.warn("Vacancy with ID {} not found for alert", vacancyId));
    }
}
