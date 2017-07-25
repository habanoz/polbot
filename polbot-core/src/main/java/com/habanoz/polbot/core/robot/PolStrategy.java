package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.Order;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.utils.ExchangePrice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by habanoz on 30.05.2017.
 */
public interface PolStrategy {
    List<Order> execute(CurrencyConfig currencyConfig, ExchangePrice priceData, BigDecimal btcBalance, BigDecimal coinBalance, Date date);

    List<PoloniexOpenOrder> getOrdersToCancel(CurrencyConfig currencyConfig, Date date);

    List<PoloniexOpenOrder> getOrdersToCancel(CurrencyConfig currencyConfig);
}
