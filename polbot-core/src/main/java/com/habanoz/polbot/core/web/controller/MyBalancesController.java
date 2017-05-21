package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.api.CoinDeskApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.User;
import com.habanoz.polbot.core.model.PoloniexCompleteBalance;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.registry.PublicRegistry;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huseyina on 4/9/2017.
 */
@Controller
public class MyBalancesController {

    public static final double EQUALS_DIFFERENCE = 0.001;
    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PublicRegistry publicRegistry;

    @Autowired
    private CoinDeskApi coinDeskApi;

    @RequestMapping({"/mybalances/{buid}"})
    public String welcome(@PathVariable Integer buid, Map<String, Object> model, Principal principal) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        //create tradingApi instance for current user
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(botUser);

        Map<String, PoloniexCompleteBalance> completeBalanceMap = tradingApi.returnCompleteBalances();
        completeBalanceMap = completeBalanceMap.entrySet().stream().filter(map -> map.getValue().getBtcValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Double allBtcProperty = completeBalanceMap.values().stream().mapToDouble(PoloniexCompleteBalance::getBtcValue).sum();


        Map<String, List<PoloniexTrade>> historyMap = tradingApi.returnTradeHistory();

        Map<String, Map<String, Object>> detailsMap = new HashMap<>();

        PublicRegistry.TickerPack tickerPack = publicRegistry.getTickerMap();

        final String prefix = "BTC_";
        for (String currency : completeBalanceMap.keySet()) {
            final String currencyPair = prefix + currency;
            List<PoloniexTrade> historyList = Optional.ofNullable(historyMap.get(currencyPair)).orElse(Collections.EMPTY_LIST);
            PoloniexCompleteBalance balance = completeBalanceMap.get(currency);
            Float available = balance.getAvailable();
            Float subAvailable = 0f;
            Float weightedSum = 0.000001f;
            for (PoloniexTrade trade : historyList) {
                if (Math.abs(available - subAvailable) <= EQUALS_DIFFERENCE)
                    break;

                if (trade.getType().equalsIgnoreCase("buy")) {
                    weightedSum += trade.getAmount().multiply(trade.getRate()).floatValue();
                    subAvailable += (1 - trade.getFee().floatValue()) * trade.getAmount().floatValue();
                }

                if (trade.getType().equalsIgnoreCase("sell")) {
                    weightedSum -= trade.getAmount().multiply(trade.getRate()).floatValue();
                    subAvailable -= trade.getAmount().floatValue();
                }
            }

            Float averagePrice = weightedSum / subAvailable;
            Float currentPrice = Optional.ofNullable(tickerPack.getTickerMap().get(currencyPair)).map(s -> s.getHighestBid().floatValue()).orElse(0f);
            Float differencePrice = (currentPrice - averagePrice);
            Float differencePricePercent = differencePrice * 100 / averagePrice;
            Float gainLossBtc = available * differencePrice;

            NumberFormat numberFormat = new DecimalFormat("#.########");
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("averagePrice", numberFormat.format(averagePrice));
            detailMap.put("currentPrice", numberFormat.format(currentPrice));
            detailMap.put("differencePricePercent", numberFormat.format(differencePricePercent));
            detailMap.put("gainLoss", numberFormat.format(gainLossBtc));

            detailsMap.put(currency, detailMap);
        }

        model.put("botUser", botUser);
        model.put("balances", completeBalanceMap);
        model.put("details", detailsMap);
        model.put("btcBalance", allBtcProperty);
        model.put("btcBalanceUsd", allBtcProperty * Optional.ofNullable(coinDeskApi.getBtcUsdPrice()).map(s -> s.getRate_float()).orElse(0f));

        return "mybalances";
    }

}
