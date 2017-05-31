package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTicker;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by huseyina on 5/29/2017.
 */
public interface PolStrategy {
    List<PoloniexOpenOrder> execute(CurrencyConfig currencyConfig, PoloniexTicker ticker, BigDecimal balance, BigDecimal budget);
}
