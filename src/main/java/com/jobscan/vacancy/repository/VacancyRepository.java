package com.jobscan.vacancy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jobscan.vacancy.entity.Vacancy;
import com.jobscan.vacancy.entity.VacancyStatus;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    boolean existsByExternalId(String externalId);

    Page<Vacancy> findByStatus(VacancyStatus status, Pageable pageable);

    Page<Vacancy> findByMatchScoreGreaterThanEqual(Integer minScore, Pageable pageable);
}
