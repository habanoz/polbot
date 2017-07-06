package com.habanoz.polbot.core.api;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.robot.CascadedPatienceStrategy;
import com.habanoz.polbot.core.robot.PatienceStrategy;
import com.habanoz.polbot.core.robot.PolBot;
import com.habanoz.polbot.core.robot.PolStrategy;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * Created by huseyina on 5/29/2017.
 */
public class ProfitabilityAnalysis {
    private static final Logger logger = LoggerFactory.getLogger(ProfitabilityAnalysis.class);

    private PoloniexPublicApi publicApi = new PoloniexPublicApiImpl();

    @Test
    public void runAnalysis() {
        try {
            analyse();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void analyse() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);

        final String currPair = "BTC_ETC";
        List<PoloniexChart> chartData = publicApi.returnChart(currPair, 300L, calendar.getTimeInMillis() / 1000, System.currentTimeMillis() / 1000);


        List<PoloniexOpenOrder> openOrders = new ArrayList<>();

        float initialBTCBalance = 0.2f;
        float initialCoinBalance = 0f;
        int balancePercent = 50;
        float buyOnPercent = 4;
        float sellOnPercent = 5;
        int cancelHour = 96;
        float volumeRatio = 0.01f;
        int dumpResponseTime = 15;

        float buyFeeRate = 0.0015f;
        float sellFeeRate = 0.0025f;

        logger.info("Starting balance btc: {}", initialBTCBalance);
        logger.info("Starting balance coin: {}", initialCoinBalance);

        Map<String, List<PoloniexTrade>> historyMap = new HashMap<>();
        historyMap.put(currPair, new ArrayList<>());


        CurrencyConfig currencyConfig = new CurrencyConfig(currPair, balancePercent, 50f, buyOnPercent, 0, sellOnPercent);
        currencyConfig.setBuyOrderCancellationHour(cancelHour);
        currencyConfig.setBuyable(true);
        currencyConfig.setSellable(true);

        BigDecimal currentBTCBalance = BigDecimal.valueOf(initialBTCBalance);
        BigDecimal currentBTCOnOrder = BigDecimal.valueOf(0);
        BigDecimal currentCoinBalance = BigDecimal.valueOf(initialCoinBalance);
        BigDecimal currentCoinOnOrder = BigDecimal.valueOf(0);

        BigDecimal lastSellPrice = new BigDecimal(0);

        for (PoloniexChart chart : chartData) {
            Date date = new Date(chart.getDate().longValue() * 1000);

            //logger.info("Day {}", date);

            List<BigDecimal> priceIndex = getPriceIndex(chart);


            for (int idx = 0; idx < priceIndex.size(); idx++) {

                BigDecimal buyPrice = priceIndex.get(idx);
                BigDecimal sellPrice = priceIndex.get(idx);
                lastSellPrice = sellPrice;

                //logger.info("Buy price {} Sell price {}", buyPrice.doubleValue(), sellPrice.doubleValue());


                OrderResult buyResultObjects = executeBuyOrders(currPair, openOrders, buyFeeRate, historyMap, buyPrice, date);
                OrderResult sellResultObjects = executeSellOrders(currPair, openOrders, sellFeeRate, historyMap, sellPrice, date);
                List<PoloniexTrade> recentTrades = new ArrayList<>();
                recentTrades.addAll(buyResultObjects.getTrades());
                recentTrades.addAll(sellResultObjects.getTrades());

                if (buyResultObjects.getResults()[0].doubleValue() != 0 || buyResultObjects.getResults()[1].doubleValue() != 0 || sellResultObjects.getResults()[0].doubleValue() != 0 || sellResultObjects.getResults()[1].doubleValue() != 0) {
                    logger.info("Buy Price {}, Sell Price", buyPrice, sellPrice);
                    logger.info("-Coin balance {} btc balance {} Coin Order {} BTC Order {}, SUM={}", currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, total(currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, sellPrice));
                }

                currentCoinBalance = currentCoinBalance.add(buyResultObjects.getResults()[0]);
                currentBTCBalance = currentBTCBalance.add(sellResultObjects.getResults()[0]);

                currentCoinOnOrder = currentCoinOnOrder.subtract(sellResultObjects.getResults()[1]);
                currentBTCOnOrder = currentBTCOnOrder.subtract(buyResultObjects.getResults()[1]);
                if (buyResultObjects.getResults()[0].doubleValue() != 0 || buyResultObjects.getResults()[1].doubleValue() != 0 || sellResultObjects.getResults()[0].doubleValue() != 0 || sellResultObjects.getResults()[1].doubleValue() != 0) {
                    logger.info("--Coin balance {} btc balance {} Coin Order {} BTC Order {}, SUM={}", currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, total(currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, sellPrice));
                }

                Map<String, List<PoloniexOpenOrder>> openOrderMap = new HashMap<>();
                openOrderMap.put(currPair, openOrders);

                PolStrategy patienceStrategy = getCascadedPatienceStrategy(historyMap.get(currPair), recentTrades, openOrderMap.get(currPair));

                ExchangePrice exchangePrice = new ExchangePrice(buyPrice, sellPrice, date, chart.getVolume());

                List<Order> orders = patienceStrategy.execute(currencyConfig, exchangePrice, currentBTCBalance.multiply(BigDecimal.valueOf(balancePercent / 100f)), currentCoinBalance, date);

                // fulfill orders
                for (Order order : orders) {
                    if (order.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {
                        currentBTCBalance = currentBTCBalance.subtract(order.getTotal());
                        currentBTCOnOrder = currentBTCOnOrder.add(order.getTotal());

                        openOrders.add(new PoloniexOpenOrder(order));

                        logger.info("Buy Price {}", buyPrice);
                        logger.info("Buy Order Given {}", order);
                        logger.info("Coin balance {} btc balance {} Coin Order {} BTC Order {}, SUM={}", currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, total(currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, sellPrice));
                    }

                    if (order.getType().equalsIgnoreCase(PolBot.SELL_ACTION)) {
                        currentCoinBalance = currentCoinBalance.subtract(order.getAmount());
                        currentCoinOnOrder = currentCoinOnOrder.add(order.getAmount());

                        openOrders.add(new PoloniexOpenOrder(order));

                        logger.info("Sell Price {}", sellPrice);
                        logger.info("Sell Order Given {}", order);
                        logger.info("Coin balance {} btc balance {} Coin Order {} BTC Order {}, SUM={}", currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, total(currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, sellPrice));
                    }
                }

                //cancel old open orders
                Iterator<PoloniexOpenOrder> openOrderIterator = patienceStrategy.getOrdersToCancel(currencyConfig, date).iterator();
                while (openOrderIterator.hasNext()) {
                    PoloniexOpenOrder openOrder = openOrderIterator.next();

                    if (openOrder.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {
                        BigDecimal order = cancelBuyOrder(openOrders, openOrder);
                        currentBTCBalance = currentBTCBalance.add(order);
                        currentBTCOnOrder = currentBTCOnOrder.subtract(order);

                        logger.info("Coin balance {} btc balance {} Coin Order {} BTC Order {}, SUM={}", currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, total(currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, sellPrice));
                    } else {
                        BigDecimal order = cancelSellOrder(openOrders, openOrder);

                        currentCoinBalance = currentCoinBalance.add(order);
                        currentCoinOnOrder = currentCoinOnOrder.subtract(order);

                        logger.info("Coin balance {} btc balance {} Coin Order {} BTC Order {}, SUM={}", currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, total(currentCoinBalance, currentBTCBalance, currentCoinOnOrder, currentBTCOnOrder, sellPrice));
                    }
                }
            }

        }

        Iterator<PoloniexOpenOrder> openOrderIterator = openOrders.iterator();
        while (openOrderIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrderIterator.next();

            if (openOrder.getType().equalsIgnoreCase(PolBot.BUY_ACTION))
                currentBTCBalance = currentBTCBalance.add(cancelBuyOrder(openOrderIterator, openOrder));
            else currentCoinBalance = currentCoinBalance.add(cancelSellOrder(openOrderIterator, openOrder));
        }


        System.out.println("coin balance=" + currentCoinBalance);
        System.out.println("btc balance=" + currentBTCBalance);
        System.out.println("Selling coins");

        currentBTCBalance = currentBTCBalance.add(sell(lastSellPrice, currentCoinBalance, sellFeeRate));

        System.out.println("ratio=" + (currentBTCBalance.divide(new BigDecimal(initialBTCBalance), BigDecimal.ROUND_CEILING)));

        System.out.println("Trades");
        for (PoloniexTrade trade : historyMap.get(currPair)) {
            System.out.println(trade);
        }

    }

    private PatienceStrategy getPatienceStrategy(List<PoloniexTrade> historyList, List<PoloniexTrade> recentTradesList, List<PoloniexOpenOrder> openOrderList) {
        return new PatienceStrategy(openOrderList, historyList);
    }

    private PatienceStrategy getCascadedPatienceStrategy(List<PoloniexTrade> historyList, List<PoloniexTrade> recentTradesList, List<PoloniexOpenOrder> openOrderList) {
        return new CascadedPatienceStrategy(openOrderList, recentTradesList, historyList);
    }

    private BigDecimal total(BigDecimal currentCoinBalance, BigDecimal currentBTCBalance, BigDecimal currentCoinOnOrder, BigDecimal currentBTCOnOrder, BigDecimal sellPrice) {
        return currentCoinBalance.add(currentCoinOnOrder).multiply(sellPrice).add(currentBTCBalance).add(currentBTCOnOrder);
    }

    private BigDecimal cancelSellOrder(List<PoloniexOpenOrder> openOrders, PoloniexOpenOrder openOrderToCancel) {
        logger.info("Sell Order Cancelled {}", openOrderToCancel);
        openOrders.remove(openOrderToCancel);
        return openOrderToCancel.getAmount();
    }

    private BigDecimal cancelBuyOrder(List<PoloniexOpenOrder> openOrders, PoloniexOpenOrder openOrderToCancel) {
        logger.info("Buy Order Cancelled {}", openOrderToCancel);
        openOrders.remove(openOrderToCancel);
        return openOrderToCancel.getTotal();
    }

    private BigDecimal cancelSellOrder(Iterator<PoloniexOpenOrder> openOrders, PoloniexOpenOrder openOrderToCancel) {
        logger.info("Sell Order Cancelled {}", openOrderToCancel);
        openOrders.remove();
        return openOrderToCancel.getAmount();
    }

    private BigDecimal cancelBuyOrder(Iterator<PoloniexOpenOrder> openOrders, PoloniexOpenOrder openOrderToCancel) {
        logger.info("Buy Order Cancelled {}", openOrderToCancel);
        openOrders.remove();
        return openOrderToCancel.getTotal();
    }

    @Test
    public void testGetPriceIndex() {
        PoloniexChart poloniexChart = new PoloniexChart(new BigDecimal(0.72), new BigDecimal(0.65), new BigDecimal(0.70), new BigDecimal(0.64), new BigDecimal(123));
        System.out.println(getPriceIndex(poloniexChart));
    }

    private List<BigDecimal> getPriceIndex(PoloniexChart chart) {
        final BigDecimal open = chart.getOpen();
        final BigDecimal close = chart.getClose();
        final BigDecimal high = chart.getHigh();
        final BigDecimal low = chart.getLow();

        final int ruler = 10;
        final List<BigDecimal> priceIndex = new ArrayList<>();

        //dumping
        if (open.doubleValue() > close.doubleValue()) {
            final BigDecimal totalRange = high.subtract(open).add(high.subtract(low).add(close.subtract(low)));

            if (totalRange.doubleValue() == 0) {
                return new ArrayList<>(Collections.singletonList(open));
            }

            final BigDecimal step = totalRange.divide(new BigDecimal(ruler), BigDecimal.ROUND_CEILING);

            int c1 = high.subtract(open).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c2 = high.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c3 = close.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();


            for (int i = 0; i < c1; i++)
                priceIndex.add(open.add(step.multiply(new BigDecimal(i))));//open->high

            for (int i = 1; i <= c2; i++)
                priceIndex.add(high.subtract(step.multiply(new BigDecimal(i))));//high->low

            for (int i = 1; i <= c3; i++)
                priceIndex.add(low.add(step.multiply(new BigDecimal(i))));//low->close

        } else {
            final BigDecimal totalRange = open.subtract(low).add(high.subtract(low).add(high.subtract(close)));

            if (totalRange.doubleValue() == 0) {
                return new ArrayList<>(Collections.singletonList(open));
            }


            final BigDecimal step = totalRange.divide(BigDecimal.valueOf(ruler), BigDecimal.ROUND_CEILING);

            int c1 = open.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c2 = high.subtract(low).divide(step, BigDecimal.ROUND_CEILING).intValue();
            int c3 = high.subtract(close).divide(step, BigDecimal.ROUND_CEILING).intValue();


            for (int i = 0; i < c1; i++)
                priceIndex.add(open.subtract(step.multiply(new BigDecimal(i))));//open->low

            for (int i = 1; i <= c2; i++)
                priceIndex.add(low.add(step.multiply(new BigDecimal(i)))); //low->high

            for (int i = 1; i <= c3; i++)
                priceIndex.add(high.subtract(step.multiply(new BigDecimal(i))));//high->close
        }

        return priceIndex;
    }

    private OrderResult executeSellOrders(String currPair, List<PoloniexOpenOrder> openOrders, float sellFeeRate, Map<String, List<PoloniexTrade>> historyMap, BigDecimal sellPrice, Date date) {
        BigDecimal gained = new BigDecimal(0);
        BigDecimal ordersCompleted = new BigDecimal(0);
        List<PoloniexTrade> trades = new ArrayList<>();

        Iterator<PoloniexOpenOrder> openOrdersIterator = openOrders.iterator();
        while (openOrdersIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrdersIterator.next();
            if (openOrder.getType().equalsIgnoreCase(PolBot.SELL_ACTION)) {
                if (openOrder.getRate().doubleValue() <= sellPrice.doubleValue()) {

                    //TODO consider volume
                    PoloniexTrade poloniexTrade = new PoloniexTrade(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()), sellPrice, openOrder.getAmount(), new BigDecimal(sellFeeRate), PolBot.SELL_ACTION);

                    historyMap.get(currPair).add(poloniexTrade);

                    // apply fees and update btc balance
                    gained = gained.add(openOrder.getTotal().multiply(new BigDecimal(1 - sellFeeRate)));
                    ordersCompleted = ordersCompleted.add(openOrder.getAmount());
                    trades.add(new PoloniexTrade(sellPrice, openOrder.getAmount(), PolBot.BUY_ACTION));

                    openOrdersIterator.remove();

                    logger.info("Sell Trade Completed {}", poloniexTrade);
                    logger.info("Sell Trade Completed at price {}", sellPrice.doubleValue());
                }
            }
        }

        return new OrderResult(trades, new BigDecimal[]{gained, ordersCompleted});
    }

    public BigDecimal sell(BigDecimal sellPrice, BigDecimal amount, float sellFeeRate) {
        return amount.multiply(sellPrice).multiply(new BigDecimal(1 - sellFeeRate));
    }

    private OrderResult executeBuyOrders(String currPair, List<PoloniexOpenOrder> openOrders, float buyFeeRate, Map<String, List<PoloniexTrade>> historyMap, BigDecimal buyPrice, Date date) {
        BigDecimal gained = new BigDecimal(0);
        BigDecimal ordersCompleted = new BigDecimal(0);
        List<PoloniexTrade> trades = new ArrayList<>();

        Iterator<PoloniexOpenOrder> openOrdersIterator = openOrders.iterator();
        while (openOrdersIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrdersIterator.next();
            if (openOrder.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {
                if (openOrder.getRate().floatValue() >= buyPrice.floatValue()) {

                    //TODO consider volume
                    PoloniexTrade poloniexTrade = new PoloniexTrade(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()), buyPrice, openOrder.getAmount(), new BigDecimal(buyFeeRate), PolBot.BUY_ACTION);

                    historyMap.get(currPair).add(poloniexTrade);

                    // apply fees and update coin balance
                    gained = gained.add(openOrder.getAmount().multiply(new BigDecimal(1 - buyFeeRate)));
                    trades.add(new PoloniexTrade(buyPrice, openOrder.getAmount(), PolBot.BUY_ACTION));
                    ordersCompleted = ordersCompleted.add(openOrder.getTotal());

                    openOrdersIterator.remove();

                    logger.info("Buy Trade Completed {}", poloniexTrade);
                    logger.info("Buy Trade Completed at price {}", buyPrice.doubleValue());
                }
            }
        }
        return new OrderResult(trades, new BigDecimal[]{gained, ordersCompleted});
    }

    class OrderResult {
        private List<PoloniexTrade> trades;
        private BigDecimal[] results;

        public OrderResult(List<PoloniexTrade> trades, BigDecimal[] results) {
            this.trades = trades;
            this.results = results;
        }

        public List<PoloniexTrade> getTrades() {
            return trades;
        }

        public BigDecimal[] getResults() {
            return results;
        }
    }
}
