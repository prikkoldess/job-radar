package com.jobscan;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.jobscan.matching.SkillExtractor;

public class SkillExtractorTest {
    private SkillExtractor skillExtractor;

    @BeforeEach
    void setUp() {
        skillExtractor = new SkillExtractor();
    }

    @Test
    void shouldExtractExistingSkillsFromText() {
        String text = "We are looking for a developer with knowledge of Java 21, Spring Boot, and a basic understanding of PostgreSQL and Docker.";
        Set<String> extractedSkills = skillExtractor.extractSkills(text);

        assertThat(extractedSkills)
                .hasSize(6)
                .containsExactlyInAnyOrder("Java", "Java 21", "Spring", "Spring Boot", "PostgreSQL", "Docker");
    }

    @Test
    void shouldNotExtractPartialMatches() {
        String text = "Знание JavaScript и React.";
        Set<String> extractedSkills = skillExtractor.extractSkills(text);

        assertThat(extractedSkills).isEmpty();

    }

    @Test
    void shouldReturnEmptySetForNullOrBlankText() {
        assertThat(skillExtractor.extractSkills(null)).isEmpty();
        assertThat(skillExtractor.extractSkills("   ")).isEmpty();
    }
}
