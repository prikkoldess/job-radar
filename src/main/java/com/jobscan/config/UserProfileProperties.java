package com.jobscan.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "user.profile")
public class UserProfileProperties {

    private List<String> skills = new ArrayList<>();

    private List<String> titleStopWords = new ArrayList<>();

    private List<String> descriptionStopWords = new ArrayList<>();

    private int matchThreshold = 75;
}
