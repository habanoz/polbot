package com.habanoz.polbot.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PoloniexTrade {
    private Long globalTradeID;
    private String tradeID;
    private LocalDateTime date;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal total;
    private BigDecimal fee;
    private String orderNumber;
    private String type;
    private String category;

    public PoloniexTrade(BigDecimal rate, BigDecimal amount, String type) {
        this.rate = rate.setScale(12, BigDecimal.ROUND_DOWN);
        this.amount = amount.setScale(12, BigDecimal.ROUND_DOWN);
        this.type = type;
        this.total = rate.multiply(amount).setScale(12, BigDecimal.ROUND_DOWN);
    }

    public PoloniexTrade(LocalDateTime date, BigDecimal rate, BigDecimal amount, BigDecimal fee, String type) {
        this(rate, amount, type);
        this.date = date;
        this.fee = fee;
    }

    public PoloniexTrade() {
    }

    public Long getGlobalTradeID() {
        return globalTradeID;
    }

    public void setGlobalTradeID(Long globalTradeID) {
        this.globalTradeID = globalTradeID;
    }

    public String getTradeID() {
        return tradeID;
    }

    public void setTradeID(String tradeID) {
        this.tradeID = tradeID;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "PoloniexTrade{" +
                "globalTradeID=" + globalTradeID +
                ", tradeID='" + tradeID + '\'' +
                ", date=" + date +
                ", rate=" + rate +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
}
