package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.Order;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTrade;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trading bot with EMA Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
public class EmaStrategy extends AbstractPolBotStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);
    public static final int WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;
    TimeSeries timeSeries;

    public EmaStrategy(CurrencyConfig currencyConfig, List<PoloniexChart> chartData, int timeFrame) {
        super(currencyConfig, chartData, timeFrame);

        List<Tick> ticks = chartData.stream().map(e -> new Tick(new DateTime(e.getDate().longValue()), e.getOpen().doubleValue(), e.getHigh().doubleValue(), e.getLow().doubleValue(), e.getClose().doubleValue(), e.getVolume().doubleValue())).collect(Collectors.toList());
        timeSeries = new TimeSeries("price series", ticks);
    }

    @Override
    public List<Order> execute(PoloniexChart chart, BigDecimal btcBalance, BigDecimal coinBalance, List<PoloniexOpenOrder> openOrderList, List<PoloniexTrade> tradeHistory, List<PoloniexTrade> recentTradeHistory) {

        String currPair = currencyConfig.getCurrencyPair();
        Date date = new Date(chart.getDate().longValue());


        List<Order> poloniexOrders = new ArrayList<>();

        timeSeries.addTick(new Tick(new DateTime(chart.getDate().longValue()), chart.getOpen().doubleValue(), chart.getHigh().doubleValue(), chart.getLow().doubleValue(), chart.getClose().doubleValue(), chart.getVolume().doubleValue()));

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);

        EMAIndicator emaIndicator = new EMAIndicator(closePriceIndicator, timeFrame);


        PercentOverIndicatorRule overIndicatorRule = new PercentOverIndicatorRule(emaIndicator, closePriceIndicator, currencyConfig.getBuyOnPercent());
        PercentBelowIndicatorRule belowIndicatorRule = new PercentBelowIndicatorRule(emaIndicator, closePriceIndicator, currencyConfig.getSellOnPercent());

        Strategy buySellSignals = new Strategy(
                overIndicatorRule,
                belowIndicatorRule
        );

        int endIndex = timeSeries.getEnd();

        //
        //
        // buy logic
        if (currencyConfig.getUsableBalancePercent() > 0 &&
                currencyConfig.getBuyable() &&
                openOrderList.stream().noneMatch(r -> r.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) &&
                buySellSignals.shouldEnter(endIndex)) {

            BigDecimal price = new BigDecimal(emaIndicator.getValue(endIndex).toDouble());
            Order openOrder = createBuyOrder(currencyConfig, currPair, price, btcBalance, date);

            if (openOrder != null)
                poloniexOrders.add(openOrder);

        }

        //
        //
        // sell logic
        if (currencyConfig.getSellable() && coinBalance.doubleValue() > minAmount) {
            BigDecimal price = new BigDecimal(closePriceIndicator.getValue(endIndex).toDouble());

            Order openOrder = createSellOrder(currencyConfig, currPair, coinBalance, price, recentTradeHistory, date);
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
                if (System.currentTimeMillis()-history.getDate().getTime()> WEEK_IN_MILLIS)
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
        BigDecimal buyPrice = lowestBuyPrice;//currencyConfig.getBuyAtPrice() == 0 ? lowestBuyPrice.multiply(new BigDecimal(1).subtract(BigDecimal.valueOf(currencyConfig.getBuyOnPercent() * 0.01))) : new BigDecimal(currencyConfig.getBuyAtPrice());

        // calculate amount that can be bought with buyBudget and buyPrice
        BigDecimal buyCoinAmount = buyBudgetInBtc.divide(buyPrice, RoundingMode.DOWN);

        return new Order(currPair, PolBot.BUY_ACTION, buyPrice, buyCoinAmount, date);
    }


}
