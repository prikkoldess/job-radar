package com.jobscan.vacancy.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jobscan.matching.model.MatchResult;
import com.jobscan.scraper.common.ScrapedVacancy;
import com.jobscan.vacancy.dto.VacancyResponseDto;
import com.jobscan.vacancy.entity.Company;
import com.jobscan.vacancy.entity.Vacancy;
import com.jobscan.vacancy.entity.VacancyStatus;
import com.jobscan.vacancy.repository.CompanyRepository;
import com.jobscan.vacancy.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VacancyService {
    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public Vacancy saveScrapedVacancy(ScrapedVacancy scrapedData, MatchResult matchResult) {
        companyRepository.insertIfNotExists(scrapedData.companyName());
        Company company = companyRepository.findByName(scrapedData.companyName())
                .orElseThrow(() -> new IllegalStateException("Company not found after insert"));

        if (company.isBlocked()) {
            log.info("Company {} is blocked, skipping vacancy {}", company.getName(), scrapedData.externalId());
            return null;
        }

        Vacancy vacancy = new Vacancy(
                scrapedData.externalId(),
                scrapedData.source(),
                scrapedData.url(),
                scrapedData.title(),
                company,
                scrapedData.description(),
                scrapedData.salaryFork(),
                matchResult.score(),
                matchResult.skillsBreakdown(),
                scrapedData.languages(),
                scrapedData.applicationsCount(),
                scrapedData.publishedAt());

        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public void updateStatus(Long vacancyId, VacancyStatus status) {
        vacancyRepository.findById(vacancyId).ifPresent(vacancy -> {
            vacancy.setStatus(status);
            log.info("Vacancy {} status updated to {}", vacancyId, status);
        });
    }

    @Transactional(readOnly = true)
    public List<Vacancy> getNewVacanciesList(Integer minScore) {
        return vacancyRepository.findByMatchScoreGreaterThanEqualAndStatusOrderByPublishedAtDesc(minScore,
                VacancyStatus.NEW);
    }

    @Transactional(readOnly = true)
    public Optional<Vacancy> getVacancyByExternalId(String externalId) {
        return vacancyRepository.findByExternalId(externalId);
    }

    public List<Vacancy> getTopNewVacancy(Integer minScore) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return vacancyRepository.findByMatchScoreGreaterThanEqualAndStatus(minScore, VacancyStatus.NEW, pageable)
                .getContent();
    }

    @Transactional(readOnly = true)
    public Map<VacancyStatus, Long> getVacancyStats() {
        return vacancyRepository.countVacanciesByStatus().stream()
                .collect(Collectors.toMap(
                        row -> (VacancyStatus) row[0],
                        row -> (Long) row[1]));
    }

    @Transactional(readOnly = true)
    public Long getCompanyIdByVacancy(Long vacancyId) {
        return vacancyRepository.findById(vacancyId)
                .map(vacancy -> vacancy.getCompany().getId())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<VacancyResponseDto> getVacancies(Pageable pageable) {
        return vacancyRepository.findAll(pageable).map(VacancyResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<VacancyResponseDto> getVacanciesByStatus(VacancyStatus status, Pageable pageable) {
        return vacancyRepository.findByStatus(status, pageable).map(VacancyResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<VacancyResponseDto> getVacanciesByMinScore(Integer minScore, Pageable pageable) {
        return vacancyRepository.findByMatchScoreGreaterThanEqual(minScore, pageable)
                .map(VacancyResponseDto::fromEntity);
    }
}
