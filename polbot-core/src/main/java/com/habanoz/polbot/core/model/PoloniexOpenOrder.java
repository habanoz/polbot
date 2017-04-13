package com.habanoz.polbot.core.model;

import java.math.BigDecimal;

public class PoloniexOpenOrder {
    private String orderNumber;
    private String currencyPair;
    private String type;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal total;

    public PoloniexOpenOrder(String currencyPair, String type, BigDecimal rate, BigDecimal amount) {
        this.currencyPair = currencyPair;
        this.type = type;
        this.rate = rate.setScale(12,BigDecimal.ROUND_DOWN);
        this.amount = amount.setScale(12,BigDecimal.ROUND_DOWN);
        this.total = rate.multiply(amount).setScale(12,BigDecimal.ROUND_DOWN);
    }

    public PoloniexOpenOrder() {
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    @Override
    public String toString() {
        return "PoloniexOpenOrder{" +
                "orderNumber='" + orderNumber + '\'' +
                ", currencyPair='" + currencyPair + '\'' +
                ", type='" + type + '\'' +
                ", rate=" + rate +
                ", amount=" + amount +
                ", total=" + total +
                '}';
    }
}
