package com.jobscan.vacancy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobscan.vacancy.repository.CompanyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Transactional
    public void blockCompany(Long companyId) {
        companyRepository.findById(companyId).ifPresentOrElse(
                company -> {
                    company.block();
                    log.info("Company {} has been blocked", company.getName());
                },
                () -> log.warn("Cannot block company: ID {} not found", companyId));
    }
}
