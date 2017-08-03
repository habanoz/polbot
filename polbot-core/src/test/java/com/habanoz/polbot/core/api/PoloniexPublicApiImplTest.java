package com.habanoz.polbot.core.api;

import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.entity.UserBot;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.repository.BotRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.repository.UserBotRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by huseyina on 6/11/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PoloniexPublicApiImplTest {

    public static final String BTC_ETC="BTC_ETC";


    @Autowired
    private UserBotRepository userBotRepository;

    @Autowired
    private BotRepository botRepository;


    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;


    @Test
    public void returnTradeHistory() throws Exception {
        PoloniexPublicApiImpl poloniexPublicApi=new PoloniexPublicApiImpl();
        long end = System.currentTimeMillis() / 1000;
        long start = end - 60 * 10;

        List<PoloniexTrade> tradeHistory = poloniexPublicApi.returnTradeHistory(BTC_ETC, start, end);

    }

    @Test
    public void getCurrencyConfig()
    {
        List<BotUser> activeBotUsers = userBotRepository.findByBotQuery("PoloniexTradeTrackerBot").stream().map(UserBot::getUser).collect(Collectors.toList());
        for (BotUser user: activeBotUsers)
        {
            List<CurrencyConfig> currencyConfigs = currencyConfigRepository.findByBotUser(user)
                    .stream().filter(r -> r.getBuyable() || r.getSellable())
                    .sorted((f1, f2) -> Float.compare(f1.getUsableBalancePercent(), f2.getUsableBalancePercent()))
                    .collect(Collectors.toList());
                System.out.println(user.getUserEmail());
                System.out.println("----------------------");
            for (CurrencyConfig currency: currencyConfigs) {
                System.out.println(currency);
            }

        }


    }

}