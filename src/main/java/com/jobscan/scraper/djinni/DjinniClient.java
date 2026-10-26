package com.jobscan.scraper.djinni;

import java.io.IOException;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.jobscan.scraper.common.RateLimiterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DjinniClient {
    private final RateLimiterService rateLimiterService;
    private static final String SOURCE_NAME = "djinni";

    public Optional<Document> fetchPage(String url) {

        rateLimiterService.acquire(SOURCE_NAME);

        try {
            log.debug("Fetching Djinni vacancy page: {}", url);

            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9,ru;q=0.8,uk;q=0.7")
                    .timeout(10000)
                    .get();
            return Optional.of(doc);
        } catch (IOException e) {
            log.error("Failed to fetch Djinni vacancy page: {}. Error: {}", url, e.getMessage());
            return Optional.empty();
        }
    }
}
