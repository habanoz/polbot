package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

@CacheConfig(cacheNames = "users")
public interface UserRepository
        extends JpaRepository<User, Integer> {

    @Cacheable
    User findByUserName(String userName);

    @CacheEvict(allEntries = true)
    @Override
    User save(User user);

    @CacheEvict(allEntries = true)
    @Override
    void delete(User user);


}
