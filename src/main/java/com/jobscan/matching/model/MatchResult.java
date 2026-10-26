package com.jobscan.matching.model;

public record MatchResult(
                int score,
                SkillsBreakdown skillsBreakdown,
                boolean hasStopWords) {
}