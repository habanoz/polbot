package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by huseyina on 4/7/2017.
 */

public interface CurrencyConfigRepository extends JpaRepository<CurrencyConfig, String> {
}
