package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexOrderResult;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.model.PoloniexTradeResult;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huseyina on 4/9/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MailServiceImplTest {

    @Autowired
    MailService mailService;

    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private BotUserRepository botUserRepository;

    @Test
    public void sendMail() throws Exception {
        mailService.sendMail("huseyinabanox@gmail.com", "merab", "naber", true);
    }


    @Test
    public void findByUserIdAndBuyableOrSellable() throws Exception {
        int userId = 1;
        List<CurrencyConfig> currencyConfigs = currencyConfigRepository.findByUserIdAndBuyableOrSellable(userId, true, true);
        int count = currencyConfigs.size();
    }

    @Test
    public void saveAllCurrencies() throws Exception {

        Map<String, PoloniexTicker> tickers = publicApi.returnTicker();
        List<BotUser>  botUsers =  this.botUserRepository.findAll();
//        for (BotUser botUser:botUsers
//             ) {
//            for (Map.Entry<String, PoloniexTicker> entry : tickers.entrySet()) {
//                List<CurrencyConfig>  c = currencyConfigRepository.findByUserId(botUser.getUserId()).stream().filter(r->r.getCurrencyPair().equals((entry.getKey()))).collect(Collectors.toList());
//
//
//if(c.size() == 0){
//    System.out.println(entry.getKey());
//    CurrencyConfig currencyConfig = new CurrencyConfig();
//    currencyConfig.setBuyable(false);
//    currencyConfig.setBuyOnPercent(10);
//    currencyConfig.setBuyAtPrice(0);
//    currencyConfig.setSellable(false);
//    currencyConfig.setSellAtPrice(0);
//    currencyConfig.setSellOnPercent(10);
//    currencyConfig.setCurrencyPair(entry.getKey());
//    currencyConfig.setUserId(botUser.getUserId());
//    currencyConfigRepository.save(currencyConfig);
//}
//
//            }
//        }



    }
}