package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by huseyina on 4/7/2017.
 */

//@CacheConfig(cacheNames = "currencyConfigs")
public interface CurrencyConfigRepository extends JpaRepository<CurrencyConfig, Integer> {
    //@Cacheable
    @Override
    List<CurrencyConfig> findAll();

    //@Cacheable
    List<CurrencyConfig> findByUserId(Integer userId);

    //@Cacheable
    List<CurrencyConfig> findByUserIdAndBuyableOrSellable(Integer userId, Boolean buyable, Boolean sellable);

    //@Cacheable

    CurrencyConfig findByUserIdAndCurrencyPair(Integer userId, String currencyPair);

    @Override
    @CacheEvict(allEntries = true)
    CurrencyConfig save(CurrencyConfig var1);

    @Override
    @CacheEvict(allEntries = true)
    void delete(CurrencyConfig var1);
}
