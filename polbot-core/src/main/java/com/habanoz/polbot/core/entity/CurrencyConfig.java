package com.habanoz.polbot.core.entity;

import javax.persistence.*;

/**
 * Created by habanoz on 05.04.2017.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "currencyPair"}))
public class CurrencyConfig {
    private Integer currencyConfigId;
    private String currencyPair;
    private float usableBalancePercent;
    private float buyAtPrice = 0;
    private float buyOnPercent;
    private float sellAtPrice = 0;
    private float sellOnPercent;
    private boolean buyable;
    private boolean sellable;
    private Integer userId;
    private Integer buyOrderCancellationHour=0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getCurrencyConfigId() {
        return currencyConfigId;
    }

    public void setCurrencyConfigId(Integer currencyConfigId) {
        this.currencyConfigId = currencyConfigId;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public float getUsableBalancePercent() {
        return usableBalancePercent;
    }

    public void setUsableBalancePercent(float usableBalancePercent) {
        this.usableBalancePercent = usableBalancePercent;
    }

    public float getSellOnPercent() {
        return sellOnPercent;
    }

    public void setSellOnPercent(float sellOnPercent) {
        this.sellOnPercent = sellOnPercent;
    }

    public float getBuyOnPercent() {
        return buyOnPercent;
    }

    public void setBuyOnPercent(float buyOnPercent) {
        this.buyOnPercent = buyOnPercent;
    }

    public boolean getBuyable() {
        return buyable;
    }

    public void setBuyable(boolean buyable) {
        this.buyable = buyable;
    }

    public boolean getSellable() {
        return sellable;
    }

    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }

    public float getBuyAtPrice() {
        return buyAtPrice;
    }

    public void setBuyAtPrice(float buyAtPrice) {
        this.buyAtPrice = buyAtPrice;
    }

    public float getSellAtPrice() {
        return sellAtPrice;
    }

    public void setSellAtPrice(float sellAtPrice) {
        this.sellAtPrice = sellAtPrice;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrencyConfig that = (CurrencyConfig) o;

        if (Float.compare(that.usableBalancePercent, usableBalancePercent) != 0) return false;
        if (Float.compare(that.buyAtPrice, buyAtPrice) != 0) return false;
        if (Float.compare(that.buyOnPercent, buyOnPercent) != 0) return false;
        if (Float.compare(that.sellAtPrice, sellAtPrice) != 0) return false;
        if (Float.compare(that.sellOnPercent, sellOnPercent) != 0) return false;
        if (buyable != that.buyable) return false;
        if (sellable != that.sellable) return false;
        if (currencyConfigId != null ? !currencyConfigId.equals(that.currencyConfigId) : that.currencyConfigId != null)
            return false;
        if (currencyPair != null ? !currencyPair.equals(that.currencyPair) : that.currencyPair != null) return false;
        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        int result = currencyConfigId != null ? currencyConfigId.hashCode() : 0;
        result = 31 * result + (currencyPair != null ? currencyPair.hashCode() : 0);
        result = 31 * result + (usableBalancePercent != +0.0f ? Float.floatToIntBits(usableBalancePercent) : 0);
        result = 31 * result + (buyAtPrice != +0.0f ? Float.floatToIntBits(buyAtPrice) : 0);
        result = 31 * result + (buyOnPercent != +0.0f ? Float.floatToIntBits(buyOnPercent) : 0);
        result = 31 * result + (sellAtPrice != +0.0f ? Float.floatToIntBits(sellAtPrice) : 0);
        result = 31 * result + (sellOnPercent != +0.0f ? Float.floatToIntBits(sellOnPercent) : 0);
        result = 31 * result + (buyable ? 1 : 0);
        result = 31 * result + (sellable ? 1 : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }

    public Integer getBuyOrderCancellationHour() {
        return buyOrderCancellationHour;
    }

    public void setBuyOrderCancellationHour(Integer buyOrderCancellationHour) {
        this.buyOrderCancellationHour = buyOrderCancellationHour;
    }
}
