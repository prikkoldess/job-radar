package com.jobscan.config;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bot_users")
@Getter
@Setter
@NoArgsConstructor
public class BotUser {
    @Id
    private Long chatId;
    @Column(name = "match_threshold", nullable = false)
    private Integer matchThreshold;

    @Column(name = "djinni_url", nullable = false, columnDefinition = "TEXT")
    private String djinniUrl;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public BotUser(Long chatId, Integer matchThreshold, String djinniUrl) {
        this.chatId = chatId;
        this.matchThreshold = matchThreshold;
        this.djinniUrl = djinniUrl;
    }

}
