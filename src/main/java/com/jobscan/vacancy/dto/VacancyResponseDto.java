package com.jobscan.vacancy.dto;

import java.time.Instant;
import java.util.Map;

import com.jobscan.matching.model.SkillsBreakdown;
import com.jobscan.vacancy.entity.Vacancy;
import com.jobscan.vacancy.entity.VacancyStatus;

public record VacancyResponseDto(
        Long id,
        String externalId,
        String source,
        String url,
        String title,
        String companyName,
        String description,
        String salaryFork,
        Integer matchScore,
        SkillsBreakdown skillsBreakdown,
        VacancyStatus status,
        Map<String, String> languages,
        Integer applicationsCount,
        Instant publishedAt) {

    public static VacancyResponseDto fromEntity(Vacancy vacancy) {
        return new VacancyResponseDto(
                vacancy.getId(),
                vacancy.getExternalId(),
                vacancy.getSource(),
                vacancy.getUrl(),
                vacancy.getTitle(),
                vacancy.getCompany().getName(),
                vacancy.getDescription(),
                vacancy.getSalaryFork(),
                vacancy.getMatchScore(),
                vacancy.getSkillsBreakdown(),
                vacancy.getStatus(),
                vacancy.getLanguages(),
                vacancy.getApplicationsCount(),
                vacancy.getPublishedAt());
    }
}
