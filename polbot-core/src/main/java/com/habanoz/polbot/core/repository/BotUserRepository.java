package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Yuce on 4/9/2017.
 */
public interface BotUserRepository
        extends JpaRepository<BotUser,Integer> {
}
