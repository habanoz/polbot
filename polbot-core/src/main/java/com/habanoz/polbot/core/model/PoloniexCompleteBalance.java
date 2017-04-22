package com.habanoz.polbot.core.model;

public class PoloniexCompleteBalance
{
    private Float  available;
    private Float  onOrders;
    private Float  btcValue;

    public PoloniexCompleteBalance() {
    }

    public PoloniexCompleteBalance(Float available, Float onOrders, Float btcValue) {
        this.available = available;
        this.onOrders = onOrders;
        this.btcValue = btcValue;
    }

    public Float getAvailable() {
        return available;
    }

    public void setAvailable(Float available) {
        this.available = available;
    }

    public Float getOnOrders() {
        return onOrders;
    }

    public void setOnOrders(Float onOrders) {
        this.onOrders = onOrders;
    }

    public Float getBtcValue() {
        return btcValue;
    }

    public void setBtcValue(Float btcValue) {
        this.btcValue = btcValue;
    }

    @Override
    public String toString() {
        return "PoloniexCompleteBalance{" +
                "available=" + available +
                ", onOrders=" + onOrders +
                ", btcValue=" + btcValue +
                '}';
    }
}
