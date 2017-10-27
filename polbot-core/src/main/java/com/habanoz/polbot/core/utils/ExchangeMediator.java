package com.habanoz.polbot.core.utils;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.Order;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.robot.PatienceStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by huseyina on 6/4/2017.
 */
public class ExchangeMediator {
    public static void main(String[] args) {
        new ExchangeMediator().execute();
    }

    public void execute() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);

        BigDecimal initialBtcBalance = BigDecimal.valueOf(0.1);

        //currency configuration
        CurrencyConfig currencyConfig = new CurrencyConfig("BTC_ETC", 50.0f, 0f, 10.0f, 0f, 6.0f);
        currencyConfig.setBuyable(true);
        currencyConfig.setSellable(true);

        //initialize exchange
        Exchange exchange = new Exchange(initialBtcBalance, 0.0015f, 0.0025f);
        exchange.init(currencyConfig, calendar.getTime(), 300L);

        //initialize strategy
        PatienceStrategy patienceStrategy = new PatienceStrategy(currencyConfig, exchange.getChartData(), 0);

        //run exchange loop
        while (exchange.hasMore()) {
            // proceed the exchange
            PoloniexChart priceData = exchange.proceed();

            Date date = new Date(priceData.getDate().longValue());

            // get next orders from strategy
            List<Order> orders = patienceStrategy.execute(priceData, exchange.getCurrentBTCBalance(), exchange.getCurrentCoinBalance(), exchange.getOpenOrders(), exchange.getHistoryData(), Collections.emptyList());

            // fulfill orders
            exchange.addOrders(orders);

            // get orders to cancel
            List<PoloniexOpenOrder> orders2Cancel = patienceStrategy.getOrdersToCancel(exchange.getOpenOrders(), date);

            // cancel orders
            exchange.cancelOrders(orders2Cancel);
        }

        exchange.cancelAllOrders();

        System.out.println("Ratio:" + exchange.getCurrentBTCBalance().divide(initialBtcBalance, RoundingMode.CEILING));

        System.out.println("Trades");
        for (PoloniexTrade trade : exchange.getHistoryData()) {
            System.out.println(trade);
        }
    }
}
