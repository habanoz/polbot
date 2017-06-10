package com.habanoz.polbot.core.utils;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexPublicApiImpl;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.robot.PolBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by huseyina on 6/3/2017.
 */
public class Exchange {
    private static final Logger logger = LoggerFactory.getLogger(Exchange.class);
    private BigDecimal initialBtcBalance;
    private List<PoloniexTrade> historyData;
    private List<PoloniexOpenOrder> openOrders;


    //internal
    BigDecimal currentBTCBalance;
    private float buyFeeRate;
    private float sellFeeRate;
    BigDecimal currentBTCOnOrder = BigDecimal.valueOf(0);
    BigDecimal currentCoinBalance = BigDecimal.valueOf(0);
    BigDecimal currentCoinOnOrder = BigDecimal.valueOf(0);
    private Long periodInSec;
    private int currentIndex;
    private CurrencyConfig currencyConfig;
    private List<ExchangePrice> exchangeData;
    private PoloniexPublicApi publicApi = new PoloniexPublicApiImpl();
    public static final int TICKS = 10;

    public Exchange(BigDecimal btcBalance, float buyFeeRate, float sellFeeRate) {
        this.initialBtcBalance = btcBalance;
        this.buyFeeRate = buyFeeRate;
        this.sellFeeRate = sellFeeRate;
    }

    public void init(CurrencyConfig currencyConfig, Date from, Long periodInSec) {
        this.currencyConfig = currencyConfig;
        this.periodInSec = periodInSec;

        List<PoloniexChart> chartData = publicApi.returnChart(currencyConfig.getCurrencyPair(), periodInSec, from.getTime() / 1000, System.currentTimeMillis() / 1000);
        exchangeData = generateExchangeData(chartData);

        historyData = new ArrayList<>();
        openOrders = new ArrayList<>();

        this.currentBTCBalance = this.initialBtcBalance;
        this.currentBTCOnOrder = BigDecimal.valueOf(0);
        this.currentCoinBalance = BigDecimal.valueOf(0);
        this.currentCoinOnOrder = BigDecimal.valueOf(0);

        currentIndex = -1;
    }

    private List<ExchangePrice> generateExchangeData(List<PoloniexChart> chartData) {
        List<ExchangePrice> priceData = new ArrayList<>();

        for (PoloniexChart poloniexChart : chartData) {
            priceData.addAll(getPriceIndex(poloniexChart));
        }

        return priceData;
    }

    public boolean hasMore() {
        return currentIndex + 1 < exchangeData.size();
    }

    public ExchangePrice proceed() {
        currentIndex++;

        if (currentIndex >= exchangeData.size())
            throw new RuntimeException("Data not available, call hasMore before proceed");

        ExchangePrice priceData = exchangeData.get(currentIndex);

        Date date = priceData.getDate();


        BigDecimal buyPrice = priceData.getBuyPrice();
        BigDecimal sellPrice = priceData.getSellPrice();

        BigDecimal[] buyResults = executeBuyOrders(openOrders, buyFeeRate, historyData, buyPrice, date);
        BigDecimal[] sellResults = executeSellOrders(openOrders, sellFeeRate, historyData, sellPrice, date);

        currentCoinBalance = currentCoinBalance.add(buyResults[0]);
        currentBTCBalance = currentBTCBalance.add(sellResults[0]);

        currentCoinOnOrder = currentCoinOnOrder.subtract(sellResults[1]);
        currentBTCOnOrder = currentBTCOnOrder.subtract(buyResults[1]);

        return priceData;
    }

    public void addOrders(List<PoloniexOpenOrder> orders) {

        for (PoloniexOpenOrder order : orders) {
            if (order.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {
                currentBTCBalance = currentBTCBalance.subtract(order.getTotal());
                currentBTCOnOrder = currentBTCOnOrder.add(order.getTotal());

                openOrders.add(order);
            }

            if (order.getType().equalsIgnoreCase(PolBot.SELL_ACTION)) {
                currentCoinBalance = currentCoinBalance.subtract(order.getAmount());
                currentCoinOnOrder = currentCoinOnOrder.add(order.getAmount());

                openOrders.add(order);
            }
        }
    }

    private List<ExchangePrice> getPriceIndex(PoloniexChart chart) {
        final BigDecimal open = chart.getOpen();
        final BigDecimal close = chart.getClose();
        final BigDecimal high = chart.getHigh();
        final BigDecimal low = chart.getLow();

        final List<ExchangePrice> priceIndex = new ArrayList<>();

        //dumping
        if (open.doubleValue() > close.doubleValue()) {
            final BigDecimal totalRange = high.subtract(open).add(high.subtract(low)).add(close.subtract(low));

            if (totalRange.doubleValue() == 0) {
                priceIndex.add(new ExchangePrice(open, open, new Date(chart.getDate().longValue() * 1000), chart.getVolume()));
                return priceIndex;
            }

            final BigDecimal step = totalRange.divide(new BigDecimal(TICKS), BigDecimal.ROUND_CEILING);

            int c1 = high.subtract(open).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c2 = high.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c3 = close.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();


            for (int i = 0; i < c1; i++)
                priceIndex.add(new ExchangePrice(open.add(step.multiply(new BigDecimal(i))),
                        new Date(1000 * (chart.getDate().longValue() + periodInSec * (i + 1) / TICKS)),
                        chart.getVolume().divide(BigDecimal.valueOf(TICKS), BigDecimal.ROUND_CEILING)));//open->high

            for (int i = 1; i <= c2; i++)
                priceIndex.add(new ExchangePrice(high.subtract(step.multiply(new BigDecimal(i))),
                        new Date(1000 * (chart.getDate().longValue() + periodInSec * (i + 1 + c1) / TICKS)),
                        chart.getVolume().divide(BigDecimal.valueOf(TICKS), BigDecimal.ROUND_CEILING)));//high->low

            for (int i = 1; i <= c3; i++)
                priceIndex.add(new ExchangePrice(low.add(step.multiply(new BigDecimal(i))),
                        new Date(1000 * (chart.getDate().longValue() + periodInSec * (i + 1 + c1 + c2) / TICKS)),
                        chart.getVolume().divide(BigDecimal.valueOf(TICKS), BigDecimal.ROUND_CEILING)));//low->close

        } else {
            final BigDecimal totalRange = open.subtract(low).add(high.subtract(low)).add(high.subtract(close));

            if (totalRange.doubleValue() == 0) {
                priceIndex.add(new ExchangePrice(open, new Date(chart.getDate().longValue() * 1000), chart.getVolume()));
                return priceIndex;
            }


            final BigDecimal step = totalRange.divide(BigDecimal.valueOf(TICKS), BigDecimal.ROUND_CEILING);

            int c1 = open.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c2 = high.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c3 = high.subtract(close).divide(step, BigDecimal.ROUND_CEILING).intValue();


            for (int i = 0; i < c1; i++)
                priceIndex.add(new ExchangePrice(open.subtract(step.multiply(new BigDecimal(i))),
                        new Date(1000 * (chart.getDate().longValue() + periodInSec * (i + 1) / TICKS)),
                        chart.getVolume().divide(BigDecimal.valueOf(TICKS), BigDecimal.ROUND_CEILING)));//open->low

            for (int i = 1; i <= c2; i++)
                priceIndex.add(new ExchangePrice(low.add(step.multiply(new BigDecimal(i))),
                        new Date(1000 * (chart.getDate().longValue() + periodInSec * (i + 1 + c1) / TICKS)),
                        chart.getVolume().divide(BigDecimal.valueOf(TICKS), BigDecimal.ROUND_CEILING))); //low->high

            for (int i = 1; i <= c3; i++)
                priceIndex.add(new ExchangePrice(high.subtract(step.multiply(new BigDecimal(i))),
                        new Date(1000 * (chart.getDate().longValue() + periodInSec * (i + 1 + c1 + c2) / TICKS)),
                        chart.getVolume().divide(BigDecimal.valueOf(TICKS), BigDecimal.ROUND_CEILING)));//high->close
        }

        return priceIndex;
    }


    private BigDecimal[] executeSellOrders(List<PoloniexOpenOrder> openOrders, float sellFeeRate, List<PoloniexTrade> historyMap, BigDecimal sellPrice, Date date) {
        BigDecimal gained = new BigDecimal(0);
        BigDecimal ordersCompleted = new BigDecimal(0);
        Iterator<PoloniexOpenOrder> openOrdersIterator = openOrders.iterator();
        while (openOrdersIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrdersIterator.next();
            if (openOrder.getType().equalsIgnoreCase(PolBot.SELL_ACTION)) {
                if (openOrder.getRate().doubleValue() <= sellPrice.doubleValue()) {

                    //TODO consider volume
                    PoloniexTrade poloniexTrade = new PoloniexTrade(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()), sellPrice, openOrder.getAmount(), new BigDecimal(sellFeeRate), PolBot.SELL_ACTION);

                    historyMap.add(poloniexTrade);

                    // apply fees and update btc balance
                    gained = gained.add(openOrder.getTotal().multiply(new BigDecimal(1 - sellFeeRate)));
                    ordersCompleted = ordersCompleted.add(openOrder.getAmount());

                    openOrdersIterator.remove();

                    logger.info("Sell Trade Completed {}", poloniexTrade);
                    logger.info("Sell Trade Completed at price {}", sellPrice.doubleValue());
                }
            }
        }

        return new BigDecimal[]{gained, ordersCompleted};
    }

    public BigDecimal sell(BigDecimal sellPrice, BigDecimal amount, float sellFeeRate) {
        return amount.multiply(sellPrice).multiply(new BigDecimal(1 - sellFeeRate));
    }

    private BigDecimal[] executeBuyOrders(List<PoloniexOpenOrder> openOrders, float buyFeeRate, List<PoloniexTrade> historyData, BigDecimal buyPrice, Date date) {
        BigDecimal gained = new BigDecimal(0);
        BigDecimal ordersCompleted = new BigDecimal(0);
        Iterator<PoloniexOpenOrder> openOrdersIterator = openOrders.iterator();
        while (openOrdersIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrdersIterator.next();
            if (openOrder.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {
                if (openOrder.getRate().floatValue() >= buyPrice.floatValue()) {

                    //TODO consider volume
                    PoloniexTrade poloniexTrade = new PoloniexTrade(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()), buyPrice, openOrder.getAmount(), new BigDecimal(buyFeeRate), PolBot.BUY_ACTION);

                    historyData.add(poloniexTrade);

                    // apply fees and update coin balance
                    gained = gained.add(openOrder.getAmount().multiply(new BigDecimal(1 - buyFeeRate)));
                    ordersCompleted = ordersCompleted.add(openOrder.getTotal());

                    openOrdersIterator.remove();

                    logger.info("Buy Trade Completed {}", poloniexTrade);
                    logger.info("Buy Trade Completed at price {}", buyPrice.doubleValue());
                }
            }
        }
        return new BigDecimal[]{gained, ordersCompleted};
    }

    public BigDecimal getCurrentBTCBalance() {
        return currentBTCBalance;
    }

    public List<PoloniexTrade> getHistoryData() {
        return historyData;
    }

    public List<PoloniexOpenOrder> getOpenOrders() {
        return openOrders;
    }

    public BigDecimal getCurrentBTCOnOrder() {
        return currentBTCOnOrder;
    }

    public BigDecimal getCurrentCoinBalance() {
        return currentCoinBalance;
    }

    public BigDecimal getCurrentCoinOnOrder() {
        return currentCoinOnOrder;
    }

    public void cancelAllOrders() {
        cancelOrders(new ArrayList<>(openOrders));
    }

    public void cancelOrders(List<PoloniexOpenOrder> orders2Cancel) {

        for (PoloniexOpenOrder order : orders2Cancel) {

            if (order.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {

                openOrders.remove(order);
                BigDecimal orderAmount = order.getTotal();

                currentBTCBalance = currentBTCBalance.add(orderAmount);
                currentBTCOnOrder = currentBTCOnOrder.subtract(orderAmount);

            } else {
                openOrders.remove(order);
                BigDecimal orderAmount = order.getAmount();

                currentCoinBalance = currentCoinBalance.add(orderAmount);
                currentCoinOnOrder = currentCoinOnOrder.subtract(orderAmount);

            }
        }
    }

    public void sell(BigDecimal amount, float sellFeeRate) {
        currentBTCBalance = currentBTCBalance.add(amount.multiply(exchangeData.get(currentIndex).getSellPrice()).multiply(new BigDecimal(1 - sellFeeRate)));
        currentCoinBalance = currentCoinBalance.subtract(amount);
    }
}