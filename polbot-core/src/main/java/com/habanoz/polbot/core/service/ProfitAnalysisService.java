package com.habanoz.polbot.core.service;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.robot.PolStrategy;
import com.habanoz.polbot.core.utils.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by huseyina on 6/4/2017.
 */
@Service
public class ProfitAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(ProfitAnalysisService.class);

    public static final float SELL_FEE_RATE = 0.0025f;
    public static final float BUY_FEE_RATE = 0.0015f;

    public Map<String, Object> execute(AnalysisConfig ac, float btcBalance) {
        Calendar startDateCalendar = Calendar.getInstance();
        startDateCalendar.add(Calendar.DAY_OF_MONTH, -1 * ac.getStartDaysAgo());

        BigDecimal initialBtcBalance = BigDecimal.valueOf(btcBalance);

        CurrencyConfig currencyConfig = new CurrencyConfig(ac.getCurrencyPair(), 100, ac.getBuyAtPrice(), ac.getBuyOnPercent(), ac.getSellAtPrice(), ac.getSellOnPercent());
        currencyConfig.setSellable(true);
        currencyConfig.setBuyable(true);

        //initialize exchange
        Exchange exchange = new Exchange(initialBtcBalance, BUY_FEE_RATE, SELL_FEE_RATE);
        exchange.init(currencyConfig, startDateCalendar.getTime(), ac.getPeriodInSec());

        Date lastExecutionDate = new Date(0L);

        //initialize strategy
        PolStrategy patienceStrategy = null;
        try {
            patienceStrategy = (PolStrategy) Class.forName("com.habanoz.polbot.core.robot." + ac.getBotName()).getConstructors()[0].newInstance(currencyConfig, exchange.getChartData(), 0);
        } catch (Exception e) {
            logger.error("Error at bot creation", e);
            return null;
        }

        //run exchange loop
        while (exchange.hasMore()) {
            // proceed the exchange
            PoloniexChart priceData = exchange.proceed();
            Date date = new Date(priceData.getDate().longValue());

            // get next orders from strategy
            List<Order> orders = patienceStrategy.execute(priceData, exchange.getCurrentBTCBalance(), exchange.getCurrentCoinBalance(), exchange.getOpenOrders(), exchange.getHistoryData(), exchange.getHistoryData(lastExecutionDate));

            // fulfill orders
            exchange.addOrders(orders);

            // get orders to cancel
            List<PoloniexOpenOrder> orders2Cancel = patienceStrategy.getOrdersToCancel(exchange.getOpenOrders(), date);

            // cancel orders
            exchange.cancelOrders(orders2Cancel);

            lastExecutionDate = date;
        }

        exchange.cancelAllOrders();

        exchange.sell(exchange.getCurrentCoinBalance(), SELL_FEE_RATE);

        System.out.println("Ratio:" + exchange.getCurrentBTCBalance().divide(initialBtcBalance, RoundingMode.CEILING));

        System.out.println("Trades");
        for (PoloniexTrade trade : exchange.getHistoryData()) {
            System.out.println(trade);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("history", exchange.getHistoryData());
        resultMap.put("ratio", exchange.getCurrentBTCBalance().divide(initialBtcBalance, RoundingMode.CEILING));

        return resultMap;
    }
}
