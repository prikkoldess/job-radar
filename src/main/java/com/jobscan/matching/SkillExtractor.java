package com.jobscan.matching;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class SkillExtractor {
    private static final Set<String> KNOWN_TECH_KEYWORDS = Set.of(
            // Languages
            "Java", "Java 8", "Java 11", "Java 17", "Java 21", "Java 25",

            // Frameworks (Java)
            "Spring", "Spring Boot", "Spring MVC", "Spring Data", "Spring Data JPA", "Spring Security",
            "Spring Cloud", "Spring WebFlux", "Hibernate", "JPA", "JDBC", "Quarkus", "Micronaut",

            // Databases & Migrations
            "SQL", "PostgreSQL", "MySQL", "Oracle", "MS SQL", "MongoDB", "Cassandra", "Elasticsearch",
            "Redis", "Memcached", "Flyway", "Liquibase", "NoSQL", "JSONB",

            // Messaging & Event Streaming
            "RabbitMQ", "Kafka", "ActiveMQ", "SQS", "SNS", "JMS", "Event Sourcing",

            // Architecture & Integration
            "REST", "REST API", "GraphQL", "gRPC", "SOAP", "Microservices", "Monolith", "WebSocket",
            "Apache Camel", "Tomcat", "Netty",

            // Security
            "JWT", "OAuth2", "OIDC", "Keycloak", "SSO",

            // DevOps, CI/CD, Cloud
            "Docker", "Docker Compose", "Kubernetes", "K8s", "Helm", "AWS", "GCP", "Azure",
            "CI/CD", "Jenkins", "GitLab CI", "GitHub Actions", "Terraform", "Ansible",

            // Tools, OS, Testing
            "Git", "Linux", "Bash", "Gradle", "JUnit", "Mockito", "Testcontainers",
            "JIRA", "Confluence", "Swagger", "OpenAPI");

    private static final Map<String, Pattern> SKILL_PATTERNS = KNOWN_TECH_KEYWORDS.stream()
            .collect(Collectors.toMap(
                    skill -> skill,
                    skill -> Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE)));

    public Set<String> extractSkills(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        return SKILL_PATTERNS.entrySet().stream()
                .filter(entry -> entry.getValue().matcher(text).find())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}