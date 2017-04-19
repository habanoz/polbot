package com.habanoz.polbot.core.model;

import java.util.List;

/**
 * Created by huseyina on 4/7/2017.
 */
public class PoloniexTradeResult {
    private String orderNumber;
    private List<PoloniexTrade> resultingTrades;

    public PoloniexTradeResult(List<PoloniexTrade> resultingTrades) {
        this.resultingTrades = resultingTrades;
    }

    public PoloniexTradeResult() {
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<PoloniexTrade> getResultingTrades() {
        return resultingTrades;
    }

    public void setResultingTrades(List<PoloniexTrade> resultingTrades) {
        this.resultingTrades = resultingTrades;
    }


    @Override
    public String toString() {
        return "PoloniexTradeResult{" +
                "orderNumber='" + orderNumber + '\'' +
                ", resultingTrades=" + resultingTrades +
                '}';
    }
}
