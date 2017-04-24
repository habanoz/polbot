package com.habanoz.polbot.core.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.model.*;
import org.apache.http.NameValuePair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 02.04.2017.
 */
public interface PoloniexTradingApi {
    PoloniexOrderResult buy(PoloniexOpenOrder order);

    PoloniexOrderResult sell(PoloniexOpenOrder order);

    Map runCommand(String commandName, TypeReference typeReference);

    Map runCommand(String commandName, List<NameValuePair> params, TypeReference typeReference);

    Map<String, List<PoloniexOpenOrder>> returnOpenOrders();

    Map<String, BigDecimal> returnBalances();

    Map<String, PoloniexCompleteBalance> returnCompleteBalances();

    BigDecimal returnBalance(String cur);

    Map<String, List<PoloniexTrade>> returnTradeHistory();

    Map<String, List<PoloniexTrade>> returnTradeHistory(Long start);

    boolean cancelOrder(String orderNumber);
}
