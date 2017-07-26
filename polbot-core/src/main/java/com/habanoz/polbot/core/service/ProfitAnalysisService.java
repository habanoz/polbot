package com.habanoz.polbot.core.service;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.AnalysisConfig;
import com.habanoz.polbot.core.model.Order;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.robot.PatienceStrategy;
import com.habanoz.polbot.core.utils.Exchange;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by huseyina on 6/4/2017.
 */
@Service
public class ProfitAnalysisService {

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

        //initialize strategy
        PatienceStrategy patienceStrategy = new PatienceStrategy(exchange.getOpenOrders(), exchange.getHistoryData());

        //run exchange loop
        while (exchange.hasMore()) {
            // proceed the exchange
            ExchangePrice priceData = exchange.proceed();

            // get next orders from strategy
            List<Order> orders = patienceStrategy.execute(currencyConfig, priceData, exchange.getCurrentBTCBalance(), exchange.getCurrentCoinBalance(), priceData.getDate());

            // fulfill orders
            exchange.addOrders(orders);

            // get orders to cancel
            List<PoloniexOpenOrder> orders2Cancel = patienceStrategy.getOrdersToCancel(currencyConfig, priceData.getDate());

            // cancel orders
            exchange.cancelOrders(orders2Cancel);
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
