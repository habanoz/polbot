package com.habanoz.polbot.core.entity;

/**
 * Created by Yuce on 5/12/2017.
 */
public class CurrencyCollectiveOrder {
    private String currencyPair;
    private String orderType;
    private String topPriceStr = "0";
    private String bottomPriceStr = "0";
    private int priceSplitter;
    private double totalBtcAmount;

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }


    public int getPriceSplitter() {
        return priceSplitter;
    }

    public void setPriceSplitter(int priceSplitter) {
        this.priceSplitter = priceSplitter;
    }

    public double getTotalBtcAmount() {
        return totalBtcAmount;
    }

    public void setTotalBtcAmount(double totalBtcAmount) {
        this.totalBtcAmount = totalBtcAmount;
    }

    public String getTopPriceStr() {
        return topPriceStr;
    }

    public void setTopPriceStr(String topPriceStr) {
        this.topPriceStr = topPriceStr;
    }

    public String getBottomPriceStr() {
        return bottomPriceStr;
    }

    public void setBottomPriceStr(String bottomPriceStr) {
        this.bottomPriceStr = bottomPriceStr;
    }
}