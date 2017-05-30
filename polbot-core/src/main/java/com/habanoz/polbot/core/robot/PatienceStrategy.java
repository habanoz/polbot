package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.entity.CurrencyOrder;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.repository.CurrencyOrderRepository;
import com.habanoz.polbot.core.repository.UserBotRepository;
import com.habanoz.polbot.core.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Trading bot with Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
public class PatienceStrategy implements PolStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);

    private static final double minAmount = 0.0001;
    private static final String CURR_PAIR_SEPARATOR = "_";
    private Map<String, List<PoloniexOpenOrder>> openOrderMap;
    private Map<String, List<PoloniexTrade>> historyMap;

    public PatienceStrategy(Map<String, List<PoloniexOpenOrder>> openOrderMap, Map<String, List<PoloniexTrade>> historyMap) {
        this.openOrderMap = openOrderMap;
        this.historyMap = historyMap;
    }


    @Override
    public List<PoloniexOpenOrder> execute(CurrencyConfig currencyConfig, PoloniexTicker ticker, BigDecimal balance, BigDecimal budget) {
        String currPair = currencyConfig.getCurrencyPair();
        final String[] currParts = currPair.split(CURR_PAIR_SEPARATOR);
        final String currName = currParts[1];
        final String marketName = currParts[0];


        List<PoloniexOpenOrder> openOrderListForCurr = openOrderMap.get(currPair);

        // this may indicate invalid currency name
        if (ticker == null)
            return Collections.emptyList();

        return runStrategy(currencyConfig, currPair, balance, openOrderListForCurr, ticker, budget);
    }

    private List<PoloniexOpenOrder> runStrategy(CurrencyConfig currencyConfig, String currPair, BigDecimal currBalance, List<PoloniexOpenOrder> openOrderListForCurr, PoloniexTicker ticker, BigDecimal buyBudget) {
        List<PoloniexOpenOrder> poloniexOrders = new ArrayList<>();

        //current lowest market price
        BigDecimal lowestBuyPrice = ticker.getLowestAsk();
        BigDecimal highestSellPrice = ticker.getHighestBid();


        //
        //
        // buy logic
        if (currencyConfig.getUsableBalancePercent() > 0 &&
                currencyConfig.getBuyable() &&
                openOrderListForCurr.stream().noneMatch(r -> r.getType().equalsIgnoreCase(PolBot.BUY_ACTION))) {

            PoloniexOpenOrder openOrder = createBuyOrder(currencyConfig, currPair, lowestBuyPrice, buyBudget);

            if (openOrder != null)
                poloniexOrders.add(openOrder);

        }

        //
        //
        // sell logic
        if (currencyConfig.getSellable() && currBalance.doubleValue() > minAmount) {
            List<PoloniexTrade> currHistoryList = historyMap.get(currPair);

            PoloniexOpenOrder openOrder = createSellOrder(currencyConfig, currPair, currBalance, highestSellPrice, currHistoryList);
            if (openOrder != null)
                poloniexOrders.add(openOrder);
        }

        return poloniexOrders;
    }

    private PoloniexOpenOrder createSellOrder(CurrencyConfig currencyConfig,
                                              String currPair,
                                              BigDecimal currBalance,
                                              BigDecimal highestSellPrice,
                                              List<PoloniexTrade> currHistoryList) {

        // get last buying price to calculate selling price
        BigDecimal lastBuyPrice = getBuyPrice(highestSellPrice, currHistoryList);

        //selling price should be a little higher to make profit
        // if set, sell at price will be used, otherwise sell on percent will be used
        BigDecimal sellPrice = currencyConfig.getSellAtPrice() == 0 ? new BigDecimal(lastBuyPrice.doubleValue() * (100 + currencyConfig.getSellOnPercent()) * 0.01) : new BigDecimal(currencyConfig.getSellAtPrice());

        return new PoloniexOpenOrder(currPair, "SELL", sellPrice, currBalance);
    }

    private BigDecimal getBuyPrice(BigDecimal highestSellPrice, List<PoloniexTrade> currHistoryList) {
        BigDecimal lastBuyPrice = highestSellPrice;

        if (currHistoryList != null && currHistoryList.size() > 0 && currHistoryList.get(0) != null) {

            for (PoloniexTrade history : currHistoryList) {
                // if remaining history records are too old, dont use them for selling price base
                if (history.getDate().plus(1, ChronoUnit.WEEKS).isBefore(LocalDateTime.now()))
                    break;

                // use most recent buy action as sell base
                if (history.getType().equalsIgnoreCase("buy")) {
                    lastBuyPrice = history.getRate();
                    break;
                }
            }
        }
        return lastBuyPrice;
    }

    private PoloniexOpenOrder createBuyOrder(CurrencyConfig currencyConfig,
                                             String currPair,
                                             BigDecimal lowestBuyPrice, BigDecimal buyBudget) {
        // not enough budget, return 0
        if (buyBudget == null || buyBudget.doubleValue() < minAmount) {
            return null;
        }

        // buying price should be a little lower to make profit
        // if set, buy at price will be used, other wise buy on percent will be used
        BigDecimal buyPrice = currencyConfig.getBuyAtPrice() == 0 ? new BigDecimal(lowestBuyPrice.doubleValue() * (100 - currencyConfig.getBuyOnPercent()) * 0.01) : new BigDecimal(currencyConfig.getBuyAtPrice());

        // calculate amount that can be bought with buyBudget and buyPrice
        BigDecimal buyAmount = buyBudget.divide(buyPrice, RoundingMode.DOWN);

        return new PoloniexOpenOrder(currPair, PoloniexPatienceBot.BUY_ACTION, buyPrice, buyAmount);
    }
}
