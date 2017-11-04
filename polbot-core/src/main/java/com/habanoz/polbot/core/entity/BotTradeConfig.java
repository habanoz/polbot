package com.habanoz.polbot.core.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by habanoz on 05.04.2017.
 */
@Entity
@Table
public class BotTradeConfig {
    private Integer tradeConfigId;
    private String currencyPair;
    private float usableBalance;
    private BigDecimal buyAtPrice;
    private BigDecimal buyAtPriceLow;
    private BigDecimal buyAtPriceHigh;
    private int buySplitHalfCount;
    private BigDecimal sellAtPrice;
    private BigDecimal sellAtPriceLow;
    private BigDecimal sellAtPriceHigh;
    private int sellSplitHalfCount;
    private BigDecimal sellModePrice;
    private BigDecimal stopLossPrice;
    private Integer sellMode;
    private BotUser botUser;
    private float orderTimeoutInHour = 0;
    private int completed;
    private int sellOrderGiven;
    private int buyOrderGiven;
    private String status;
    private Date created=new Date();
    private Date updated=new Date();


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getTradeConfigId() {
        return tradeConfigId;
    }

    public void setTradeConfigId(Integer tradeConfigId) {
        this.tradeConfigId = tradeConfigId;
    }


    @ManyToOne
    @JoinColumn(name = "bot_user")
    public BotUser getBotUser() {
        return botUser;
    }

    public void setBotUser(BotUser botUser) {
        this.botUser = botUser;
    }


    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public float getUsableBalance() {
        return usableBalance;
    }

    public void setUsableBalance(float usableBalance) {
        this.usableBalance = usableBalance;
    }


    public float getOrderTimeoutInHour() {
        return orderTimeoutInHour;
    }

    public void setOrderTimeoutInHour(float orderTimeoutInHour) {
        this.orderTimeoutInHour = orderTimeoutInHour;
    }

    public BigDecimal getBuyAtPrice() {
        return buyAtPrice;
    }

    public void setBuyAtPrice(BigDecimal buyAtPrice) {
        this.buyAtPrice = buyAtPrice;
    }

    public BigDecimal getBuyAtPriceLow() {
        return buyAtPriceLow;
    }

    public void setBuyAtPriceLow(BigDecimal buyAtPriceLow) {
        this.buyAtPriceLow = buyAtPriceLow;
    }

    public BigDecimal getBuyAtPriceHigh() {
        return buyAtPriceHigh;
    }

    public void setBuyAtPriceHigh(BigDecimal buyAtPriceHigh) {
        this.buyAtPriceHigh = buyAtPriceHigh;
    }

    public BigDecimal getSellAtPrice() {
        return sellAtPrice;
    }

    public void setSellAtPrice(BigDecimal sellAtPrice) {
        this.sellAtPrice = sellAtPrice;
    }

    public BigDecimal getSellAtPriceLow() {
        return sellAtPriceLow;
    }

    public void setSellAtPriceLow(BigDecimal sellAtPriceLow) {
        this.sellAtPriceLow = sellAtPriceLow;
    }

    public BigDecimal getSellAtPriceHigh() {
        return sellAtPriceHigh;
    }

    public void setSellAtPriceHigh(BigDecimal sellAtPriceHigh) {
        this.sellAtPriceHigh = sellAtPriceHigh;
    }

    public BigDecimal getSellModePrice() {
        return sellModePrice;
    }

    public void setSellModePrice(BigDecimal sellModePrice) {
        this.sellModePrice = sellModePrice;
    }

    public BigDecimal getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(BigDecimal stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public Integer getSellMode() {
        return sellMode;
    }

    public void setSellMode(Integer sellMode) {
        this.sellMode = sellMode;
    }

    public int getSellSplitHalfCount() {
        return sellSplitHalfCount;
    }

    public void setSellSplitHalfCount(int sellSplitHalfCount) {
        this.sellSplitHalfCount = sellSplitHalfCount;
    }

    public int getBuySplitHalfCount() {
        return buySplitHalfCount;
    }

    public void setBuySplitHalfCount(int buySplitHalfCount) {
        this.buySplitHalfCount = buySplitHalfCount;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public String getStatus() {
        return status;
    }

    public int getSellOrderGiven() {
        return sellOrderGiven;
    }

    public void setSellOrderGiven(int sellOrderGive) {
        this.sellOrderGiven = sellOrderGive;
    }

    public int getBuyOrderGiven() {
        return buyOrderGiven;
    }

    public void setBuyOrderGiven(int buyOrderGive) {
        this.buyOrderGiven = buyOrderGive;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotTradeConfig)) return false;

        BotTradeConfig that = (BotTradeConfig) o;

        if (Float.compare(that.usableBalance, usableBalance) != 0) return false;
        if (buySplitHalfCount != that.buySplitHalfCount) return false;
        if (sellSplitHalfCount != that.sellSplitHalfCount) return false;
        if (Float.compare(that.orderTimeoutInHour, orderTimeoutInHour) != 0) return false;
        if (completed != that.completed) return false;
        if (sellOrderGiven != that.sellOrderGiven) return false;
        if (buyOrderGiven != that.buyOrderGiven) return false;
        if (tradeConfigId != null ? !tradeConfigId.equals(that.tradeConfigId) : that.tradeConfigId != null)
            return false;
        if (currencyPair != null ? !currencyPair.equals(that.currencyPair) : that.currencyPair != null) return false;
        if (buyAtPrice != null ? !buyAtPrice.equals(that.buyAtPrice) : that.buyAtPrice != null) return false;
        if (buyAtPriceLow != null ? !buyAtPriceLow.equals(that.buyAtPriceLow) : that.buyAtPriceLow != null)
            return false;
        if (buyAtPriceHigh != null ? !buyAtPriceHigh.equals(that.buyAtPriceHigh) : that.buyAtPriceHigh != null)
            return false;
        if (sellAtPrice != null ? !sellAtPrice.equals(that.sellAtPrice) : that.sellAtPrice != null) return false;
        if (sellAtPriceLow != null ? !sellAtPriceLow.equals(that.sellAtPriceLow) : that.sellAtPriceLow != null)
            return false;
        if (sellAtPriceHigh != null ? !sellAtPriceHigh.equals(that.sellAtPriceHigh) : that.sellAtPriceHigh != null)
            return false;
        if (sellModePrice != null ? !sellModePrice.equals(that.sellModePrice) : that.sellModePrice != null)
            return false;
        if (stopLossPrice != null ? !stopLossPrice.equals(that.stopLossPrice) : that.stopLossPrice != null)
            return false;
        if (sellMode != null ? !sellMode.equals(that.sellMode) : that.sellMode != null) return false;
        if (botUser != null ? !botUser.equals(that.botUser) : that.botUser != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        return updated != null ? updated.equals(that.updated) : that.updated == null;

    }

    @Override
    public int hashCode() {
        int result = tradeConfigId != null ? tradeConfigId.hashCode() : 0;
        result = 31 * result + (currencyPair != null ? currencyPair.hashCode() : 0);
        result = 31 * result + (usableBalance != +0.0f ? Float.floatToIntBits(usableBalance) : 0);
        result = 31 * result + (buyAtPrice != null ? buyAtPrice.hashCode() : 0);
        result = 31 * result + (buyAtPriceLow != null ? buyAtPriceLow.hashCode() : 0);
        result = 31 * result + (buyAtPriceHigh != null ? buyAtPriceHigh.hashCode() : 0);
        result = 31 * result + buySplitHalfCount;
        result = 31 * result + (sellAtPrice != null ? sellAtPrice.hashCode() : 0);
        result = 31 * result + (sellAtPriceLow != null ? sellAtPriceLow.hashCode() : 0);
        result = 31 * result + (sellAtPriceHigh != null ? sellAtPriceHigh.hashCode() : 0);
        result = 31 * result + sellSplitHalfCount;
        result = 31 * result + (sellModePrice != null ? sellModePrice.hashCode() : 0);
        result = 31 * result + (stopLossPrice != null ? stopLossPrice.hashCode() : 0);
        result = 31 * result + (sellMode != null ? sellMode.hashCode() : 0);
        result = 31 * result + (botUser != null ? botUser.hashCode() : 0);
        result = 31 * result + (orderTimeoutInHour != +0.0f ? Float.floatToIntBits(orderTimeoutInHour) : 0);
        result = 31 * result + completed;
        result = 31 * result + sellOrderGiven;
        result = 31 * result + buyOrderGiven;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (updated != null ? updated.hashCode() : 0);
        return result;
    }
}
