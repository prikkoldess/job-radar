package com.jobscan.vacancy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jobscan.vacancy.entity.Vacancy;
import com.jobscan.vacancy.entity.VacancyStatus;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    boolean existsByExternalId(String externalId);

    Page<Vacancy> findByStatus(VacancyStatus status, Pageable pageable);

    Page<Vacancy> findByMatchScoreGreaterThanEqual(Integer minScore, Pageable pageable);

    Page<Vacancy> findByMatchScoreGreaterThanEqualAndStatus(Integer minScore, VacancyStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "company" })
    List<Vacancy> findByMatchScoreGreaterThanEqualAndStatusOrderByPublishedAtDesc(Integer minScore,
            VacancyStatus status);

    @EntityGraph(attributePaths = { "company" })
    Optional<Vacancy> findByExternalId(String externalId);

    @Query("SELECT v.status, COUNT(v) FROM Vacancy v GROUP BY v.status")
    List<Object[]> countVacanciesByStatus();
}
