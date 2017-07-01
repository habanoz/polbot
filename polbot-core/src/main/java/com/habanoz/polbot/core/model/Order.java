package com.habanoz.polbot.core.model;

import java.math.BigDecimal;
import java.util.Date;

public class Order {
    public static final int DEFAULT = 0;
    public static final int FILL_OR_KILL = 1;
    public static final int IMMEDIATE_OR_CANCEL = 2;
    public static final int POST_ONLY = 3;

    private String orderNumber;
    private String currencyPair;
    private String type;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal total;
    private Date date;
    private int mode = 0;

    public Order(String currencyPair, String type, BigDecimal rate, BigDecimal amount) {
        this.currencyPair = currencyPair;
        this.type = type;
        this.rate = rate.setScale(12, BigDecimal.ROUND_DOWN);
        this.amount = amount.setScale(12, BigDecimal.ROUND_DOWN);
        this.total = rate.multiply(amount).setScale(12, BigDecimal.ROUND_DOWN);
    }

    public Order(String currencyPair, String type, BigDecimal rate, BigDecimal amount, Date date) {
        this(currencyPair, type, rate, amount);
        this.date = date;
    }

    public Order(String currencyPair, String type, BigDecimal rate, BigDecimal amount, Date date, int mode) {
        this(currencyPair, type, rate, amount, date);
        this.mode = mode;
    }


    public Order() {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order openOrder = (Order) o;

        if (orderNumber != null ? !orderNumber.equals(openOrder.orderNumber) : openOrder.orderNumber != null)
            return false;
        if (currencyPair != null ? !currencyPair.equals(openOrder.currencyPair) : openOrder.currencyPair != null)
            return false;
        if (type != null ? !type.equals(openOrder.type) : openOrder.type != null) return false;
        if (rate != null ? !rate.equals(openOrder.rate) : openOrder.rate != null) return false;
        if (amount != null ? !amount.equals(openOrder.amount) : openOrder.amount != null) return false;
        if (total != null ? !total.equals(openOrder.total) : openOrder.total != null) return false;
        return date != null ? date.equals(openOrder.date) : openOrder.date == null;
    }

    @Override
    public int hashCode() {
        int result = orderNumber != null ? orderNumber.hashCode() : 0;
        result = 31 * result + (currencyPair != null ? currencyPair.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (rate != null ? rate.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PoloniexOpenOrder{" +
                ", currencyPair='" + currencyPair + '\'' +
                ", type='" + type + '\'' +
                ", rate=" + rate +
                ", amount=" + amount +
                ", total=" + total +
                '}';
    }
}
