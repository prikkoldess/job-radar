package com.jobscan.scraper.common;

import java.util.Optional;

public interface JobScraper {
    String getSource();

    Optional<ScrapedVacancy> scrapeVacancy(String url);

}
