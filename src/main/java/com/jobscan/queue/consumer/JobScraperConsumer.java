package com.jobscan.queue.consumer;

import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.jobscan.config.RabbitMQConfig;
import com.jobscan.config.UserProfileProperties;
import com.jobscan.matching.MatchScoreCalculator;
import com.jobscan.matching.model.MatchResult;
import com.jobscan.queue.event.JobUrlDiscoveredEvent;
import com.jobscan.scraper.common.JobScraper;
import com.jobscan.scraper.common.ScrapedVacancy;
import com.jobscan.vacancy.entity.Vacancy;
import com.jobscan.vacancy.service.VacancyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobScraperConsumer {
    private final List<JobScraper> scrapers;
    private final MatchScoreCalculator matchScoreCalculator;
    private final VacancyService vacancyService;
    private final RabbitTemplate rabbitTemplate;
    private final UserProfileProperties userProfile;

    @RabbitListener(queues = RabbitMQConfig.URLS_QUEUE)
    public void consumeScrapeTask(JobUrlDiscoveredEvent event) {
        log.info("Processing URL from queue: {}", event.url());

        JobScraper scraper = scrapers.stream()
                .filter(s -> s.getSource().equalsIgnoreCase(event.source()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No scraper found for source: " + event.source()));

        Optional<ScrapedVacancy> scrapedOpt = scraper.scrapeVacancy(event.url());

        if (scrapedOpt.isEmpty()) {
            return;
        }

        ScrapedVacancy scrapedData = scrapedOpt.get();

        MatchResult matchResult = matchScoreCalculator.calculate(scrapedData.title(), scrapedData.description());

        if (matchResult.hasStopWords()) {
            log.info("Vacancy {} contains stop words, skipping", scrapedData.externalId());
            return;
        }

        if (matchResult.score() == 0) {
            log.info("Vacancy {} has 0 score", scrapedData.externalId());
            return;
        }
        Vacancy savedVacancy = vacancyService.saveScrapedVacancy(scrapedData, matchResult);

        if (savedVacancy != null && matchResult.score() >= userProfile.getMatchThreshold()) {
            log.info("High match score ({}%) for vacancy {}. Sending alert.", matchResult.score(),
                    savedVacancy.getId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_ALERT_HIGH_MATCH,
                    savedVacancy.getId());
        }
    }
}
