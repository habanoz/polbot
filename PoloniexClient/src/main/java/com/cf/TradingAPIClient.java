package com.cf;

import org.apache.http.NameValuePair;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author David
 */
public interface TradingAPIClient
{
    public String returnBalances();

    public String returnCompleteBalances();

    public String returnFeeInfo();
    
    public String returnOpenOrders();
    
    public String returnTradeHistory(String currencyPair);
    
    public String cancelOrder(String orderNumber);
    
    public String moveOrder(String orderNumber, BigDecimal rate);

    public String sell(String currencyPair, BigDecimal buyPrice, BigDecimal amount, boolean fillOrKill, boolean immediateOrCancel, boolean postOnly);

    public String buy(String currencyPair, BigDecimal buyPrice, BigDecimal amount, boolean fillOrKill, boolean immediateOrCancel, boolean postOnly);

    String returnTradingAPICommandResults(String commandValue);

    String returnTradingAPICommandResults(String commandValue, List<NameValuePair> params);
}
