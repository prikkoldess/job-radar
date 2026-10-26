package com.jobscan.vacancy.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.jobscan.matching.model.SkillsBreakdown;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vacancies", indexes = {
        @Index(name = "idx_vacancies_external_id", columnList = "external_id", unique = true),
        @Index(name = "idx_vacancies_status", columnList = "status"),
        @Index(name = "idx_vacancies_match_score", columnList = "match_score")
})
@Getter
@Setter
@NoArgsConstructor
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vacancies_seq_gen")
    @SequenceGenerator(name = "vacancies_seq_gen", sequenceName = "vacancies_SEQ", allocationSize = 50)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "salary_fork", nullable = false)
    private String salaryFork;

    @Column(name = "match_score", nullable = false)
    private Integer matchScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills_breakdown", columnDefinition = "jsonb")
    private SkillsBreakdown skillsBreakdown;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VacancyStatus status;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "languages", columnDefinition = "jsonb")
    private Map<String, String> languages;

    @Column(name = "applications_count")
    private Integer applicationCount;

    public Vacancy(String externalId, String source, String url, String title, Company company, String description,
            String salaryFork, int matchScore, SkillsBreakdown skillsBreakdown, Map<String, String> languages,
            Integer applicationsCount,
            Instant publishedAt) {
        this.externalId = Objects.requireNonNull(externalId, "externalId is required");
        this.source = Objects.requireNonNull(source, "source is required");
        this.url = Objects.requireNonNull(url, "url is required");
        this.title = Objects.requireNonNull(title, "title is required");
        this.company = Objects.requireNonNull(company, "company is required");
        this.description = description;
        this.salaryFork = salaryFork;
        this.matchScore = matchScore;
        this.skillsBreakdown = skillsBreakdown != null ? skillsBreakdown : SkillsBreakdown.empty();
        this.languages = languages != null ? languages : new HashMap<>();
        this.applicationCount = applicationsCount != null ? applicationsCount : 0;
        this.status = VacancyStatus.NEW;
        this.publishedAt = publishedAt;
        this.createdAt = Instant.now();
    }

}
