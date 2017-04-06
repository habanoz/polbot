package com.habanoz.polbot.core.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.habanoz.polbot.core.model.PoloniexCompleteBalance;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.model.PoloniexTradeHistory;
import org.apache.http.NameValuePair;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 02.04.2017.
 */
public interface PoloniexTradingApi {
    String buy(String currencyPair, BigDecimal buyPrice, BigDecimal amount);

    String sell(String currencyPair, BigDecimal sellPrice, BigDecimal amount);

    Map runCommand(String commandName, TypeReference typeReference);

    Map runCommand(String commandName, List<NameValuePair> params, TypeReference typeReference);

    Map<String, List<PoloniexOpenOrder>> returnOpenOrders();

    Map<String, Float> returnBalances();

    Map<String, PoloniexCompleteBalance> returnCompleteBalances();

    Float returnBalance(String cur);

    Map<String, List<PoloniexTradeHistory>> returnTradeHistory();
}
