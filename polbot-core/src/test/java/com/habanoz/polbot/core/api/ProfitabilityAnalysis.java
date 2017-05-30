package com.habanoz.polbot.core.api;

import com.cf.data.model.poloniex.PoloniexTradeHistory;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.robot.PatienceStrategy;
import com.habanoz.polbot.core.robot.PolBot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        final String currPair = "BTC_DGB";
        List<PoloniexChart> chartData = publicApi.returnChart(currPair, 300L, calendar.getTimeInMillis() / 1000, System.currentTimeMillis() / 1000);


        Queue<PoloniexOpenOrder> buyOrders = new PriorityQueue<>(new Comparator<PoloniexOpenOrder>() {
            @Override
            public int compare(PoloniexOpenOrder order, PoloniexOpenOrder order2) {
                return -1 * (int) (order.getRate().doubleValue() - order2.getRate().doubleValue());
            }
        });

        Queue<PoloniexOpenOrder> sellOrders = new PriorityQueue<>(new Comparator<PoloniexOpenOrder>() {
            @Override
            public int compare(PoloniexOpenOrder order, PoloniexOpenOrder order2) {
                return (int) (order.getRate().doubleValue() - order2.getRate().doubleValue());
            }
        });

        float initialBTCBalance = 0.2f;
        float initialCoinBalance = 0f;
        int balancePercent = 50;
        float buyOnPercent = 4;
        float sellOnPercent = 4;
        float volumeRatio = 0.01f;
        int dumpResponseTime = 15;

        float buyFeeRate = 0.015f;
        float sellFeeRate = 0.025f;

        logger.info("Starting balance btc: {}", initialBTCBalance);
        logger.info("Starting balance coin: {}", initialCoinBalance);

        Map<String, List<PoloniexTrade>> historyMap = new HashMap<>();
        historyMap.put(currPair, new ArrayList<>());


        CurrencyConfig currencyConfig = new CurrencyConfig(currPair, balancePercent, 0f, buyOnPercent, 0, sellOnPercent);
        currencyConfig.setBuyable(true);
        currencyConfig.setSellable(true);

        BigDecimal currentBTCBalance = BigDecimal.valueOf(initialBTCBalance);
        BigDecimal currentCoinBalance = BigDecimal.valueOf(initialCoinBalance);

        for (PoloniexChart chart : chartData) {

            List<BigDecimal> priceIndex = getPriceIndex(chart);


            for (int idx = 0; idx < priceIndex.size(); idx++) {

                BigDecimal buyPrice = priceIndex.get(idx);
                BigDecimal sellPrice = priceIndex.get(idx);

                currentCoinBalance = executeBuyOrders(currPair, buyOrders, buyFeeRate, historyMap, currentCoinBalance, buyPrice);
                currentBTCBalance = executeSellOrders(currPair, sellOrders, sellFeeRate, historyMap, currentBTCBalance, sellPrice);

                Map<String, List<PoloniexOpenOrder>> openOrderMap = new HashMap<>();
                openOrderMap.put(currPair, new ArrayList<>(buyOrders));
                openOrderMap.get(currPair).addAll(sellOrders);

                PatienceStrategy patienceStrategy = new PatienceStrategy(openOrderMap, historyMap);

                PoloniexTicker poloniexTicker = new PoloniexTicker(chart.getOpen(), chart.getOpen(), chart.getOpen(), new BigDecimal(0), chart.getVolume());

                List<PoloniexOpenOrder> orders = patienceStrategy.execute(currencyConfig, poloniexTicker, currentBTCBalance.multiply(BigDecimal.valueOf(balancePercent / 100f)), currentCoinBalance);

                for (PoloniexOpenOrder order : orders) {
                    if (order.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {
                        currentBTCBalance = currentBTCBalance.subtract(order.getTotal());
                        buyOrders.add(order);

                        logger.info("Buy Order Given {}", order);
                    }

                    if (order.getType().equalsIgnoreCase(PolBot.SELL_ACTION)) {
                        currentCoinBalance = currentCoinBalance.subtract(order.getAmount());
                        sellOrders.add(order);

                        logger.info("Sell Order Given {}", order);
                    }
                }
            }

        }

        System.out.println();
        System.out.println("ration=" + (currentBTCBalance.divide(new BigDecimal(initialBTCBalance), BigDecimal.ROUND_CEILING)));
        System.out.println("coind balance=" + currentCoinBalance);

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
        if (chart.getOpen().doubleValue() > chart.getClose().doubleValue()) {
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

    private BigDecimal executeSellOrders(String currPair, Queue<PoloniexOpenOrder> sellOrders, float sellFeeRate, Map<String, List<PoloniexTrade>> historyMap, BigDecimal currentBTCBalance, BigDecimal sellPrice) {
        Iterator<PoloniexOpenOrder> sellOrdersIterator = sellOrders.iterator();
        while (sellOrdersIterator.hasNext()) {
            PoloniexOpenOrder sellOrder = sellOrdersIterator.next();
            if (sellOrder.getRate().doubleValue() <= sellPrice.doubleValue()) {

                //TODO consider volume
                PoloniexTrade poloniexTrade = new PoloniexTrade(LocalDateTime.now(), sellPrice, sellOrder.getAmount(), new BigDecimal(sellFeeRate), PolBot.SELL_ACTION);

                historyMap.get(currPair).add(poloniexTrade);

                // apply fees and update btc balance
                currentBTCBalance = currentBTCBalance.add(new BigDecimal(sellOrder.getTotal().doubleValue() * (1 - sellFeeRate)));

                sellOrdersIterator.remove();

                logger.info("Sell Trade Completed {}", poloniexTrade);
                logger.info("Sell Trade Completed btc balance {}", currentBTCBalance);
            } else // orders are in price order, if last one is not executed, remainings will no be executed
                break;
        }

        return currentBTCBalance;
    }

    private BigDecimal executeBuyOrders(String currPair, Queue<PoloniexOpenOrder> buyOrders, float buyFeeRate, Map<String, List<PoloniexTrade>> historyMap, BigDecimal currentCoinBalance, BigDecimal buyPrice) {
        Iterator<PoloniexOpenOrder> buyOrdersIterator = buyOrders.iterator();
        while (buyOrdersIterator.hasNext()) {
            PoloniexOpenOrder buyOrder = buyOrdersIterator.next();
            if (buyOrder.getRate().floatValue() >= buyPrice.floatValue()) {

                //TODO consider volume
                PoloniexTrade poloniexTrade = new PoloniexTrade(LocalDateTime.now(), buyPrice, buyOrder.getAmount(), new BigDecimal(buyFeeRate), PolBot.BUY_ACTION);

                historyMap.get(currPair).add(poloniexTrade);

                // apply fees and update coin balance
                currentCoinBalance = currentCoinBalance.add(new BigDecimal(buyOrder.getAmount().doubleValue() * (1 - buyFeeRate)));

                buyOrdersIterator.remove();

                logger.info("Buy Trade Completed {}", poloniexTrade);

            } else // orders are in price order, if last one is not executed, remainings will no be executed
                break;
        }
        return currentCoinBalance;
    }
}
