package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Yuce on 4/9/2017.
 */
public interface BotUserRepository
        extends JpaRepository<BotUser,Integer> {
    List<BotUser> findByIsActive(boolean isActive);
}
