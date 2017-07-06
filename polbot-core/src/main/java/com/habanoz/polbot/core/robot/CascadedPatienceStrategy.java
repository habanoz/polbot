package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.Order;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Trading bot with Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
public class CascadedPatienceStrategy extends PatienceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);
    private final List<PoloniexTrade> filledOrderList;

    public CascadedPatienceStrategy(List<PoloniexOpenOrder> openOrderList, List<PoloniexTrade> filledOrdersList, List<PoloniexTrade> historyList) {
        super(openOrderList, historyList);
        this.filledOrderList = filledOrdersList;
    }


    @Override
    public List<Order> execute(CurrencyConfig currencyConfig, ExchangePrice priceData, BigDecimal btcBalance, BigDecimal coinBalance, Date date) {
        String currPair = currencyConfig.getCurrencyPair();

        // this may indicate invalid currency name
        if (priceData == null)
            return Collections.emptyList();

        return runStrategy(currencyConfig, currPair, btcBalance, coinBalance, openOrderList, priceData, date);
    }

    private List<Order> runStrategy(CurrencyConfig currencyConfig, String currPair, BigDecimal btcBalance, BigDecimal coinBalance, List<PoloniexOpenOrder> openOrderListForCurr, ExchangePrice priceData, Date date) {
        List<Order> poloniexOrders = new ArrayList<>();

        //current lowest market price
        BigDecimal lowestBuyPrice = priceData.getBuyPrice();

        //
        //
        // buy logic
        if (currencyConfig.getUsableBalancePercent() > 0 &&
                currencyConfig.getBuyable() &&
                openOrderListForCurr.stream().noneMatch(r -> r.getType().equalsIgnoreCase(PolBot.BUY_ACTION))) {

            Order order = createBuyOrder(currencyConfig, currPair, lowestBuyPrice, btcBalance, date);

            if (order != null)
                poloniexOrders.add(order);

        }

        //
        //
        // sell logic
        if (currencyConfig.getSellable() && coinBalance.doubleValue() > minAmount) {
            for (PoloniexTrade order : filledOrderList) {
                Order openOrder = createSellOrder(currencyConfig, currPair, order, date);
                if (openOrder != null)
                    poloniexOrders.add(openOrder);
            }
        }

        return poloniexOrders;
    }

    private Order createSellOrder(CurrencyConfig currencyConfig, String currPair, PoloniexTrade order, Date date) {

        //selling price should be a little higher to make profit
        BigDecimal sellPrice = order.getRate().multiply(new BigDecimal(1).add(BigDecimal.valueOf(currencyConfig.getSellOnPercent() * 0.01 + 0.0015)));

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
