package com.habanoz.polbot.core.entity;

/**
 * Created by Yuce on 5/12/2017.
 */
public class CurrencyCollectiveOrder
{
    private String currencyPair;
    private String orderType;
    private double topPrice;
    private double bottomPrice;
    private int priceSplitter;
    private double pricePercentSplitter;
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

    public double getTopPrice() {
        return topPrice;
    }

    public void setTopPrice(double topPrice) {
        this.topPrice = topPrice;
    }

    public double getBottomPrice() {
        return bottomPrice;
    }

    public void setBottomPrice(double bottomPrice) {
        this.bottomPrice = bottomPrice;
    }

    public int getPriceSplitter() {
        return priceSplitter;
    }

    public void setPriceSplitter(int priceSplitter) {
        this.priceSplitter = priceSplitter;
    }

    public double getPricePercentSplitter() {
        return pricePercentSplitter;
    }

    public void setPricePercentSplitter(double pricePercentSplitter) {
        this.pricePercentSplitter = pricePercentSplitter;
    }

    public double getTotalBtcAmount() {
        return totalBtcAmount;
    }

    public void setTotalBtcAmount(double totalBtcAmount) {
        this.totalBtcAmount = totalBtcAmount;
    }
}