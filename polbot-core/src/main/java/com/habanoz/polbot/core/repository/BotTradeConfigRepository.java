package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.BotTradeConfig;
import com.habanoz.polbot.core.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by huseyina on 4/7/2017.
 */

public interface BotTradeConfigRepository extends JpaRepository<BotTradeConfig, Integer> {
    @Override
    List<BotTradeConfig> findAll();

    List<BotTradeConfig> findByBotUser(BotUser botUser);

    BotTradeConfig findByBotUserAndCurrencyPair(BotUser botUser, String currencyPair);

    BotTradeConfig save(BotTradeConfig var1);

    void delete(BotTradeConfig var1);

    List<BotTradeConfig> findByBotUserAndCompleted(BotUser user, int i);
}
