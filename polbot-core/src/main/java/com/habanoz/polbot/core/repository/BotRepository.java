package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.Bot;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotRepository
        extends JpaRepository<Bot, Integer> {
    Bot findByName(String name);
}
