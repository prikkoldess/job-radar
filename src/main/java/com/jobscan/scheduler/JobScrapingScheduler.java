package com.jobscan.scheduler;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jobscan.config.RabbitMQConfig;
import com.jobscan.queue.event.JobUrlDiscoveredEvent;
import com.jobscan.scraper.deduplication.JobDeduplicationService;
import com.jobscan.scraper.djinni.DjinniClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class JobScrapingScheduler {
    private final DjinniClient djinniClient;
    private final RabbitTemplate rabbitTemplate;
    private final JobDeduplicationService deduplicationService;

    private static final String DJINNI_SEARCH_URL = "https://djinni.co/jobs/?primary_keyword=Java&exp_level=2y&employment=remote";
    private static final String BASE_URL = "https://djinni.co";
    private static final Pattern ID_PATTERN = Pattern.compile("/jobs/(\\d+)-");

    @Scheduled(fixedDelay = 1800000)
    public void scheduleDjinniScraping() {
        log.info("Starting Djinni job scraping cycle...");

        Optional<Document> pageOpt = djinniClient.fetchPage(DJINNI_SEARCH_URL);
        if (pageOpt.isEmpty()) {
            log.error("Failed to fetch Djinni search page.");
            return;
        }

        Document doc = pageOpt.get();

        Elements jobLinks = doc.select("a[href^=/jobs/]");

        int newJobsCount = 0;

        for (Element link : jobLinks) {
            String href = link.attr("href");

            if (!href.matches("^/jobs/\\d+-.*")) {
                continue;
            }

            String externalId = extractExternalId(href);
            if (externalId == null) {
                continue;
            }

            if (deduplicationService.isNew(externalId)) {
                String fullUrl = BASE_URL + href;
                log.info("Discovered new job URL: {}", fullUrl);

                JobUrlDiscoveredEvent event = new JobUrlDiscoveredEvent(fullUrl, "Djinni");
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_URL_DISCOVERED,
                        event);

                newJobsCount++;
            }
        }

        log.info("Djinni scraping cycle finished. Queued {} new jobs for processing.", newJobsCount);
    }

    private String extractExternalId(String url) {
        Matcher matcher = ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
