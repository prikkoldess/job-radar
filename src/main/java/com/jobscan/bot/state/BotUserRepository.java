package com.jobscan.bot.state;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobscan.config.BotUser;

public interface BotUserRepository extends JpaRepository<BotUser, Long> {

}
