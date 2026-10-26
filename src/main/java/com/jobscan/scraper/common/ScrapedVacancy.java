package com.jobscan.scraper.common;

import java.time.Instant;
import java.util.Map;

public record ScrapedVacancy(
                String externalId,
                String source,
                String url,
                String title,
                String companyName,
                String description,
                String salaryFork,
                Map<String, String> languages,
                Integer applicationsCount,
                Instant publishedAt) {

}
