package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.Order;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Trading bot with Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
public class CascadedPatienceStrategy extends AbstractPolBotStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);

    public CascadedPatienceStrategy(CurrencyConfig currencyConfig, List<PoloniexChart> chartData, int timeFrame) {
        super(currencyConfig, chartData, timeFrame);
    }

    @Override
    public List<Order> execute(PoloniexChart chart, BigDecimal btcBalance, BigDecimal coinBalance, List<PoloniexOpenOrder> openOrderList, List<PoloniexTrade> tradeHistory, List<PoloniexTrade> recentTradeHistory) {
        String currPair = currencyConfig.getCurrencyPair();

        List<Order> poloniexOrders = new ArrayList<>();
        Date date = new Date(chart.getDate().longValue());

        //current lowest market price
        BigDecimal lowestBuyPrice = chart.getClose();

        //
        //
        // buy logic
        if (currencyConfig.getUsableBalancePercent() > 0 &&
                currencyConfig.getBuyable() &&
                openOrderList.stream().noneMatch(r -> r.getType().equalsIgnoreCase(PolBot.BUY_ACTION))) {

            Order order = createBuyOrder(currencyConfig, currPair, lowestBuyPrice, btcBalance, date);

            if (order != null)
                poloniexOrders.add(order);

        }

        //
        //
        // sell logic
        if (currencyConfig.getSellable() && coinBalance.doubleValue() > minAmount) {
            for (PoloniexTrade order : recentTradeHistory) {
                Order openOrder = createSellOrder(currencyConfig, coinBalance, currPair, order, date);
                if (openOrder != null)
                    poloniexOrders.add(openOrder);
            }
        }

        return poloniexOrders;
    }

    private Order createSellOrder(CurrencyConfig currencyConfig, BigDecimal coinBalance, String currPair, PoloniexTrade order, Date date) {

        //selling price should be a little higher to make profit
        BigDecimal sellPrice = order.getRate().multiply(new BigDecimal(1).add(BigDecimal.valueOf(currencyConfig.getSellOnPercent() * 0.01 + 0.0015)));

        //return new Order(currPair, "SELL", sellPrice, coinBalance.min(order.getAmount()), date);
        return new Order(currPair, "SELL", sellPrice, order.getAmount(), date);
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
        BigDecimal buyPrice = lowestBuyPrice.multiply(new BigDecimal(1).subtract(BigDecimal.valueOf(currencyConfig.getBuyOnPercent() * 0.01 + 0.0025)));

        BigDecimal splitCount = BigDecimal.valueOf(currencyConfig.getBuyAtPrice());

        // calculate amount that can be bought with buyBudget and buyPrice
        BigDecimal buyCoinAmount = buyBudgetInBtc.divide(splitCount, RoundingMode.DOWN).divide(buyPrice, RoundingMode.DOWN);

        return new Order(currPair, PolBot.BUY_ACTION, buyPrice, buyCoinAmount, date);
    }
}
