package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.BotUser;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Yuce on 4/9/2017.
 */
@CacheConfig(cacheNames = "botUsers")
public interface BotUserRepository
        extends JpaRepository<BotUser, Integer> {

    @Cacheable
    @Override
    List<BotUser> findAll();

    @Cacheable
    List<BotUser> findByActive(boolean isActive);

    @CacheEvict(allEntries = true)
    @Override
    BotUser save(BotUser botUser);

    @CacheEvict(allEntries = true)
    @Override
    void delete(BotUser botUser);

    BotUser findByUserEmail(String email);

    BotUser findByUserName(String userName);
}
