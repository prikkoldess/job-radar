package com.jobscan.matching.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SkillsBreakdown(
        @JsonProperty("matched_skills") List<String> matchedSkills,
        @JsonProperty("missing_skills") List<String> missingSkills) implements Serializable {

    public SkillsBreakdown {
        matchedSkills = matchedSkills == null ? Collections.emptyList() : List.copyOf(matchedSkills);
        missingSkills = missingSkills == null ? Collections.emptyList() : List.copyOf(missingSkills);
    }

    public static SkillsBreakdown empty() {
        return new SkillsBreakdown(Collections.emptyList(), Collections.emptyList());
    }
}
