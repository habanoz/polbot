package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huseyina on 4/9/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MailServiceImplTest {


    private static final double minAmount = 0.0001;
    private static final long BUY_SELL_SLEEP = 100;
    private static final String BASE_CURR = "BTC";
    private static final String CURR_PAIR_SEPARATOR = "_";


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
    public void calculateTradingBTCForEachCurrency() throws Exception {
        int userId = 1;
        HashMap<String,BigDecimal> tradingBTCMap = new HashMap<>();
        //User specific currency config list
        BotUser user = botUserRepository.findOne(userId);
        List<CurrencyConfig> currencyConfigs = currencyConfigRepository.findByUserId(user.getUserId()).stream().filter(r -> r.getBuyable() || r.getSellable()).collect(Collectors.toList());


        //create tradingApi instance for current user
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(user);

        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();
        BigDecimal btcBalance = balanceMap.get(BASE_CURR);  // Total available balance for that cycle.
        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();

        //  while(btcBalance.doubleValue() > 0){
        while(btcBalance.doubleValue() > minAmount){  // Loop through until available BTC distributed based on its percentage or ALT_LIMIT calculation
            currencyConfigs = currencyConfigs.stream().filter(r -> r.getBuyable()).collect(Collectors.toList());  // setting is buyable
            for (CurrencyConfig currencyConfig : currencyConfigs) {


                String currPair = currencyConfig.getCurrencyPair();
                String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];

                BigDecimal currBalance = balanceMap.get(currName);

                List<PoloniexOpenOrder> openOrderListForCurr = openOrderMap.get(currPair);

                if (currencyConfig.getUsableBalancePercent() > 0 &&
                        currencyConfig.getBuyable() &&
                        !openOrderListForCurr.stream().anyMatch(r->r.getType().equalsIgnoreCase("BUY"))) {
                    // only pre-defined percentage of available balance can be used for buying a currency
                    BigDecimal buyBudget = new BigDecimal(btcBalance.doubleValue() * currencyConfig.getUsableBalancePercent() * 0.01);

                    if (buyBudget.doubleValue() < minAmount && btcBalance.doubleValue() >= minAmount) {
                        buyBudget = new BigDecimal(minAmount);

                        if(tradingBTCMap.containsKey(currName)){
                            buyBudget=buyBudget.add(tradingBTCMap.get(currName));  // Increase its limit until we do not have any BTC available.
                            tradingBTCMap.put(currName,buyBudget);
                        }else{
                            tradingBTCMap.put(currName,buyBudget);
                        }

                    }else{
                        tradingBTCMap.put(currName,buyBudget);  // Percantage calculation  is done for that currency
                        currencyConfig.setBuyable(false);     // Do not make calculation again for that currency
                    }


                    btcBalance = btcBalance.subtract(buyBudget);


                }
            }
        }



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