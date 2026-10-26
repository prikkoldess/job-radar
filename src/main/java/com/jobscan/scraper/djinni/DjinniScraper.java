package com.jobscan.scraper.djinni;

import java.util.Optional;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import com.jobscan.scraper.common.JobScraper;
import com.jobscan.scraper.common.ScrapedVacancy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DjinniScraper implements JobScraper {
    private final DjinniClient client;
    private final DjinniParser parser;

    @Override
    public String getSource() {
        return "Djinni";
    }

    @Override
    public Optional<ScrapedVacancy> scrapeVacancy(String url) {
        log.info("Scraping Djinni vacancy: {}", url);

        Optional<Document> documentOpt = client.fetchPage(url);

        if (documentOpt.isEmpty()) {
            log.warn("Failed to retrieve document for Djinni URL: {}", url);
            return Optional.empty();
        }

        return parser.parse(documentOpt.get(), url);
    }
}