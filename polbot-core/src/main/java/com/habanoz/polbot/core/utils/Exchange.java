package com.habanoz.polbot.core.utils;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexPublicApiImpl;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.Order;
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
 * simulates behaviour of an exchange
 * Maps chartdata to exchangeprice
 * <p>
 * Created by huseyina on 6/3/2017.
 */
public class Exchange {
    private static final Logger logger = LoggerFactory.getLogger(Exchange.class);
    private BigDecimal initialBtcBalance;
    private List<PoloniexTrade> historyData;
    private List<PoloniexOpenOrder> openOrders;
    private List<PoloniexChart> chartData;


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

        chartData = publicApi.returnChart(currencyConfig.getCurrencyPair(), periodInSec, from.getTime() / 1000, System.currentTimeMillis() / 1000);

        historyData = new ArrayList<>();
        openOrders = new ArrayList<>();


        this.currentBTCBalance = this.initialBtcBalance;
        this.currentBTCOnOrder = BigDecimal.valueOf(0);
        this.currentCoinBalance = BigDecimal.valueOf(0);
        this.currentCoinOnOrder = BigDecimal.valueOf(0);

        currentIndex = -1;
    }

    public boolean hasMore() {
        return currentIndex + 1 < chartData.size();
    }

    public PoloniexChart proceed() {
        currentIndex++;

        if (currentIndex >= chartData.size())
            throw new RuntimeException("Data not available, call hasMore before proceed");

        PoloniexChart priceData = chartData.get(currentIndex);
        Date date = new Date(priceData.getDate().longValue() * 1000);

        BigDecimal buyPrice = priceData.getClose();
        BigDecimal sellPrice = priceData.getClose();

        BigDecimal[] buyResults = executeBuyOrders(openOrders, buyFeeRate, historyData, buyPrice, date);
        BigDecimal[] sellResults = executeSellOrders(openOrders, sellFeeRate, historyData, sellPrice, date);

        currentCoinBalance = currentCoinBalance.add(buyResults[0]);
        currentBTCBalance = currentBTCBalance.add(sellResults[0]);

        currentCoinOnOrder = currentCoinOnOrder.subtract(sellResults[1]);
        currentBTCOnOrder = currentBTCOnOrder.subtract(buyResults[1]);

        return priceData;
    }

    public void addOrders(List<Order> orders) {

        for (Order order_ : orders) {
            PoloniexOpenOrder order = new PoloniexOpenOrder(order_);

            if (order.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {

                if (currentBTCBalance.compareTo(order.getTotal()) < 0) {
                    logger.info("BUY Order NOT added {}, insufficient funds", order);

                } else {
                    currentBTCBalance = currentBTCBalance.subtract(order.getTotal());
                    currentBTCOnOrder = currentBTCOnOrder.add(order.getTotal());

                    logger.info("BUY Order added {}", order);
                    openOrders.add(order);
                }
            }

            if (order.getType().equalsIgnoreCase(PolBot.SELL_ACTION)) {
                if (currentCoinBalance.compareTo(order.getAmount()) < 0) {
                    logger.info("SELL Order NOT added {}, insufficient funds", order);

                } else {
                    currentCoinBalance = currentCoinBalance.subtract(order.getAmount());
                    currentCoinOnOrder = currentCoinOnOrder.add(order.getAmount());

                    logger.info("SELL Order added {}", order);
                    openOrders.add(order);
                }
            }
        }
    }

    /**
     * interpolate candle data and generate continuous price index
     *
     * @param chart
     * @return
     * @deprecated do not use, no proven benefit
     */
    private List<ExchangePrice> getContinuousPriceIndex(PoloniexChart chart) {
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


    private BigDecimal[] executeSellOrders(List<PoloniexOpenOrder> openOrders, float sellFeeRate, List<PoloniexTrade> historyList, BigDecimal sellPrice, Date date) {
        BigDecimal gained = new BigDecimal(0);
        BigDecimal ordersCompleted = new BigDecimal(0);
        Iterator<PoloniexOpenOrder> openOrdersIterator = openOrders.iterator();
        while (openOrdersIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrdersIterator.next();
            if (openOrder.getType().equalsIgnoreCase(PolBot.SELL_ACTION)) {
                if (openOrder.getRate().doubleValue() <= sellPrice.doubleValue()) {

                    // apply fees and update btc balance
                    BigDecimal btcAmount=openOrder.getTotal().multiply(new BigDecimal(1 - sellFeeRate));
                    gained = gained.add(btcAmount);
                    ordersCompleted = ordersCompleted.add(openOrder.getAmount());


                    //TODO consider volume
                    PoloniexTrade poloniexTrade = new PoloniexTrade(date, sellPrice, openOrder.getAmount(), new BigDecimal(sellFeeRate), PolBot.SELL_ACTION);

                    historyList.add(poloniexTrade);

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

    //TODO assign id s to orders and trades to track them
    //TODO handle fee s
    private BigDecimal[] executeBuyOrders(List<PoloniexOpenOrder> openOrders, float buyFeeRate, List<PoloniexTrade> historyData, BigDecimal buyPrice, Date date) {
        BigDecimal gained = new BigDecimal(0);
        BigDecimal ordersCompleted = new BigDecimal(0);
        Iterator<PoloniexOpenOrder> openOrdersIterator = openOrders.iterator();

        while (openOrdersIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrdersIterator.next();
            if (openOrder.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {
                if (openOrder.getRate().floatValue() >= buyPrice.floatValue()) {

                    // apply fees and update coin balance
                    BigDecimal coinAmount=openOrder.getAmount().multiply(new BigDecimal(1 - buyFeeRate));
                    gained = gained.add(coinAmount);
                    ordersCompleted = ordersCompleted.add(openOrder.getTotal());

                    openOrdersIterator.remove();

                    PoloniexTrade poloniexTrade = new PoloniexTrade(date, buyPrice, coinAmount, new BigDecimal(buyFeeRate), PolBot.BUY_ACTION);

                    historyData.add(poloniexTrade);



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

    /**
     * return all trade history
     *
     * @return
     */
    public List<PoloniexTrade> getHistoryData() {
        return historyData;
    }

    /**
     * Return trades happening after given date
     *
     * @param since
     * @return
     */
    public List<PoloniexTrade> getHistoryData(Date since) {

        int lastSeenIndex = historyData.size() - 1;
        for (; lastSeenIndex >= 0; lastSeenIndex--)
            if (since.getTime() > historyData.get(lastSeenIndex).getDate().getTime())
                break;

        if (lastSeenIndex < 0)
            return historyData;

        if (lastSeenIndex >= historyData.size() - 1)
            return Collections.EMPTY_LIST;

        return historyData.subList(lastSeenIndex + 1, historyData.size());
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

                currentBTCBalance = currentBTCBalance.add(order.getTotal());
                currentBTCOnOrder = currentBTCOnOrder.subtract(order.getTotal());

                logger.info("BUY Order cancelled {}", order);

            } else {
                openOrders.remove(order);

                currentCoinBalance = currentCoinBalance.add(order.getAmount());
                currentCoinOnOrder = currentCoinOnOrder.subtract(order.getAmount());

                logger.info("SELL Order cancelled {}", order);
            }
        }
    }

    public void sell(BigDecimal amount, float sellFeeRate) {
        currentBTCBalance = currentBTCBalance.add(amount.multiply(chartData.get(currentIndex).getClose()).multiply(new BigDecimal(1 - sellFeeRate)));
        currentCoinBalance = currentCoinBalance.subtract(amount);
    }

    public List<PoloniexChart> getChartData() {
        if (currentIndex >= chartData.size())
            return chartData;

        if (currentIndex < 0)
            return Collections.EMPTY_LIST;

        return chartData.subList(0, currentIndex + 1);//end index is exclusive
    }
}
