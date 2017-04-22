package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.UserBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserBotRepository
        extends JpaRepository<UserBot, Integer> {

    List<UserBot> findByBot(String bot);

    @Query("select p from UserBot p where p.user.active=true and p.bot=?1")
    List<UserBot> findEnabledUsersByBotQuery(String bot);
}
