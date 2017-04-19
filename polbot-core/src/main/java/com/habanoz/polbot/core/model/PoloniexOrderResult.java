package com.habanoz.polbot.core.model;

/**
 * Created by huseyina on 4/19/2017.
 */
public class PoloniexOrderResult {
    private PoloniexOpenOrder order;
    private PoloniexTradeResult tradeResult;
    private boolean success;
    private String error;

    public PoloniexOrderResult() {
    }

    public PoloniexOrderResult(PoloniexOpenOrder order, PoloniexTradeResult tradeResult) {
        this.order = order;
        this.tradeResult = tradeResult;
        success = true;
    }

    public PoloniexOrderResult(PoloniexOpenOrder order, String error) {
        this.order = order;
        this.error = error;
        success = false;
    }

    public PoloniexOpenOrder getOrder() {
        return order;
    }

    public void setOrder(PoloniexOpenOrder order) {
        this.order = order;
    }

    public PoloniexTradeResult getTradeResult() {
        return tradeResult;
    }

    public void setTradeResult(PoloniexTradeResult tradeResult) {
        this.tradeResult = tradeResult;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
