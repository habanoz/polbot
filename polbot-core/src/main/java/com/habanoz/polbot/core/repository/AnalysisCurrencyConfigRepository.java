package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.AnalysisCurrencyConfig;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by huseyina on 4/7/2017.
 */

public interface AnalysisCurrencyConfigRepository extends JpaRepository<AnalysisCurrencyConfig, String> {
    List<AnalysisCurrencyConfig> findAll();

    List<AnalysisCurrencyConfig> findByEnabledTrue();

}
