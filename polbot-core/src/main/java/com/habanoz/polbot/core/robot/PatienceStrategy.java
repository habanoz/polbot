package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PatienceStrategy extends AbstractPolBotStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);
    public static final int WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;

    public PatienceStrategy(CurrencyConfig currencyConfig, List<PoloniexChart> chartData, int timeFrame) {
        super(currencyConfig, chartData, timeFrame);
    }

    @Override
    public List<Order> execute(PoloniexChart chart, BigDecimal btcBalance, BigDecimal coinBalance, List<PoloniexOpenOrder> openOrderList, List<PoloniexTrade> tradeHistory, List<PoloniexTrade> recentTradeHistory) {
        String currPair = currencyConfig.getCurrencyPair();
        Date date = new Date(chart.getDate().longValue());

        List<Order> poloniexOrders = new ArrayList<>();

        //current lowest market price
        BigDecimal lowestBuyPrice = chart.getClose();
        BigDecimal highestSellPrice = chart.getClose();


        //
        //
        // buy logic
        if (currencyConfig.getUsableBalancePercent() > 0 &&
                currencyConfig.getBuyable() &&
                openOrderList.stream().noneMatch(r -> r.getType().equalsIgnoreCase(PolBot.BUY_ACTION))) {

            Order openOrder = createBuyOrder(currencyConfig, currPair, lowestBuyPrice, btcBalance, date);

            if (openOrder != null)
                poloniexOrders.add(openOrder);

        }

        //
        //
        // sell logic
        if (currencyConfig.getSellable() && coinBalance.doubleValue() > minAmount) {
            Order openOrder = createSellOrder(currencyConfig, currPair, coinBalance, highestSellPrice, recentTradeHistory, date);
            if (openOrder != null)
                poloniexOrders.add(openOrder);
        }

        return poloniexOrders;
    }

    private Order createSellOrder(CurrencyConfig currencyConfig,
                                  String currPair,
                                  BigDecimal currCoinAmount,
                                  BigDecimal highestSellPrice,
                                  List<PoloniexTrade> currHistoryList, Date date) {

        // get last buying price to calculate selling price
        BigDecimal lastBuyPrice = getBuyPrice(highestSellPrice, currHistoryList);

        //selling price should be a little higher to make profit
        // if set, sell at price will be used, otherwise sell on percent will be used
        BigDecimal sellPrice = currencyConfig.getSellAtPrice() == 0 ? lastBuyPrice.multiply(new BigDecimal(1).add(BigDecimal.valueOf(currencyConfig.getSellOnPercent() * 0.01))) : new BigDecimal(currencyConfig.getSellAtPrice());

        return new Order(currPair, "SELL", sellPrice, currCoinAmount, date);
    }

    protected BigDecimal getBuyPrice(BigDecimal highestSellPrice, List<PoloniexTrade> currHistoryList) {
        BigDecimal lastBuyPrice = highestSellPrice;

        if (currHistoryList != null && currHistoryList.size() > 0 && currHistoryList.get(0) != null) {
            for (int i = currHistoryList.size() - 1; currHistoryList.size() >= 0; i--) {
                PoloniexTrade history = currHistoryList.get(i);

                // if remaining history records are too old, dont use them for selling price base
                if (System.currentTimeMillis() - history.getDate().getTime() > WEEK_IN_MILLIS)
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

    private Order createBuyOrder(CurrencyConfig currencyConfig,
                                 String currPair,
                                 BigDecimal lowestBuyPrice, BigDecimal buyBudgetInBtc, Date date) {
        // not enough budget, return 0
        if (buyBudgetInBtc == null || buyBudgetInBtc.doubleValue() < minAmount) {
            return null;
        }

        // buying price should be a little lower to make profit
        // if set, buy at price will be used, other wise buy on percent will be used
        BigDecimal buyPrice = currencyConfig.getBuyAtPrice() == 0 ? lowestBuyPrice.multiply(new BigDecimal(1).subtract(BigDecimal.valueOf(currencyConfig.getBuyOnPercent() * 0.01))) : new BigDecimal(currencyConfig.getBuyAtPrice());

        // calculate amount that can be bought with buyBudget and buyPrice
        BigDecimal buyCoinAmount = buyBudgetInBtc.divide(buyPrice, RoundingMode.DOWN);

        return new Order(currPair, PolBot.BUY_ACTION, buyPrice, buyCoinAmount, date);
    }

}
