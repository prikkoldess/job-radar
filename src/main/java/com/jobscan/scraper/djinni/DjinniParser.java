package com.jobscan.scraper.djinni;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.jobscan.scraper.common.ScrapedVacancy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DjinniParser {
    private static final Pattern ID_PATTERN = Pattern.compile("/jobs/(\\d+)-");
    private static final Pattern APPLICATIONS_PATTERN = Pattern.compile("(\\d+)\\s+відгук");
    private static final Pattern DATE_POSTED_PATTERN = Pattern.compile("\"datePosted\"\\s*:\\s*\"([^\"]+)\"");
    private static final ZoneId KYIV_ZONE = ZoneId.of("Europe/Kyiv");

    public Optional<ScrapedVacancy> parse(Document doc, String url) {
        try {
            String title = extractText(doc, "h1");
            if (title.isEmpty()) {
                log.warn("Title not found for URL: {}. Parsing failed.", url);
                return Optional.empty();
            }

            String externalId = extractExternalId(url);

            String companyName = "Unknown Company";
            Element companyElement = doc.selectFirst("a.text-secondary.fw-medium[href*=/jobs/company-]");

            if (companyElement == null) {
                companyElement = doc.selectFirst(".job-details--title");
            }

            if (companyElement != null && !companyElement.text().trim().isEmpty()) {
                companyName = companyElement.text().trim();
            }

            String description = extractText(doc, ".job-post__description");

            String salaryFork = extractText(doc, ".public-salary-item");

            Map<String, String> languages = new HashMap<>();
            Elements languageRows = doc.select(".detail-rows__line");
            for (Element row : languageRows) {
                String name = extractText(row, ".detail-rows__name");
                String value = extractText(row, ".detail-rows__value");
                if (!name.isEmpty() && !value.isEmpty()) {
                    languages.put(name, value);
                }
            }

            Integer applicationsCount = 0;
            Element applicationsElement = doc.selectFirst("span.text-nowrap:contains(відгук)");
            if (applicationsElement != null) {
                Matcher m = APPLICATIONS_PATTERN.matcher(applicationsElement.text());
                if (m.find()) {
                    applicationsCount = Integer.parseInt(m.group(1));
                }
            }

            Instant publishedAt = extractPublishedAt(doc).orElseGet(Instant::now);

            ScrapedVacancy vacancy = new ScrapedVacancy(
                    externalId,
                    "Djinni",
                    url,
                    title,
                    companyName,
                    description,
                    salaryFork,
                    languages,
                    applicationsCount,
                    publishedAt);

            return Optional.of(vacancy);
        } catch (Exception e) {
            log.error("Error parsing Djinni document for URL: {}. Exception: {}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Instant> extractPublishedAt(Document doc) {
        Element script = doc.selectFirst("script[type=application/ld+json]");
        if (script != null) {
            Matcher m = DATE_POSTED_PATTERN.matcher(script.data());
            if (m.find()) {
                try {
                    String dateStr = m.group(1);
                    return Optional.of(LocalDateTime.parse(dateStr).atZone(KYIV_ZONE).toInstant());
                } catch (Exception e) {
                    log.warn("Failed to parse datePosted from JSON-LD", e);
                }
            }
        }
        return Optional.empty();
    }

    private String extractText(Element element, String cssQuery) {
        Element target = element.selectFirst(cssQuery);
        return target != null ? target.text().trim() : "";
    }

    private String extractExternalId(String url) {
        Matcher matcher = ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
}
