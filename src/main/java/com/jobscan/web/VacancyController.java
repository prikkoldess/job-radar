package com.jobscan.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import com.jobscan.vacancy.dto.VacancyResponseDto;
import com.jobscan.vacancy.entity.VacancyStatus;
import com.jobscan.vacancy.service.VacancyService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vacancies")
@RequiredArgsConstructor
public class VacancyController {
    private final VacancyService vacancyService;

    @GetMapping
    @Operation(summary = "Get a list of vacancies")
    public Page<VacancyResponseDto> getVacancies(
            @ParameterObject @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return vacancyService.getVacancies(pageable);
    }

    @GetMapping("/status")
    @Operation(summary = "Get a list of vacancies by a specific status")
    public Page<VacancyResponseDto> getByStatus(@RequestParam VacancyStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return vacancyService.getVacanciesByStatus(status, pageable);
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Change status", description = "Updates the vacancy status (NEW, APPLIED, ARCHIVED, REJECTED)")
    public void updateStatus(@PathVariable Long vacancyId, @RequestParam VacancyStatus status) {
        vacancyService.updateStatus(vacancyId, status);
    }

    @GetMapping("/matchscore")
    @Operation(summary = "The list of vacancies is not less than the specified match score.")
    public Page<VacancyResponseDto> getVacanciesByMatchScore(@RequestParam Integer minScore,
            @ParameterObject @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return vacancyService.getVacanciesByMinScore(minScore, pageable);
    }
}
