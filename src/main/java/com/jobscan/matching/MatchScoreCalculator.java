package com.jobscan.matching;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import com.jobscan.config.UserProfileProperties;
import com.jobscan.matching.model.MatchResult;
import com.jobscan.matching.model.SkillsBreakdown;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MatchScoreCalculator {
    private final UserProfileProperties userProfile;
    private final SkillExtractor skillExtractor;

    public MatchResult calculate(String title, String description) {
        String safeTitle = title != null ? title : "";
        String safeDescription = description != null ? description : "";
        String fullText = safeTitle + " " + safeDescription;

        if (containsStopWord(safeTitle, userProfile.getTitleStopWords()) ||
                containsStopWord(fullText, userProfile.getDescriptionStopWords())) {
            return new MatchResult(0, SkillsBreakdown.empty(), true);
        }

        Set<String> requiredSkills = skillExtractor.extractSkills(fullText);
        if (requiredSkills.isEmpty()) {
            return new MatchResult(0, SkillsBreakdown.empty(), false);
        }

        List<String> targetSkills = userProfile.getSkills();
        if (targetSkills == null || targetSkills.isEmpty()) {
            return new MatchResult(0, SkillsBreakdown.empty(), false);
        }

        Set<String> mySkillsLower = targetSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<String> matchedSkills = requiredSkills.stream()
                .filter(skill -> mySkillsLower.contains(skill.toLowerCase()))
                .sorted()
                .toList();

        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !mySkillsLower.contains(skill.toLowerCase()))
                .sorted()
                .toList();

        int score = (int) Math.round((double) matchedSkills.size() / requiredSkills.size() * 100);

        return new MatchResult(score, new SkillsBreakdown(matchedSkills, missingSkills), false);
    }

    private boolean containsStopWord(String text, List<String> stopWords) {
        if (stopWords == null || stopWords.isEmpty()) {
            return false;
        }
        return stopWords.stream().anyMatch(word -> {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            return pattern.matcher(text).find();
        });
    }
}
