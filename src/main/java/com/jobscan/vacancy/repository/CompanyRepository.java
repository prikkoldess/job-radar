package com.jobscan.vacancy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jobscan.vacancy.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);

    @Modifying
    @Query(value = """
            INSERT INTO companies (name, blocked, created_at)
            VALUES (:name, false, NOW())
            ON CONFLICT (name) DO NOTHING
            """, nativeQuery = true)
    void insertIfNotExists(@Param("name") String name);
}
