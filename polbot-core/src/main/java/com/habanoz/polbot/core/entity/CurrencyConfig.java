package com.habanoz.polbot.core.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by habanoz on 05.04.2017.
 */
@Entity
public class CurrencyConfig {
    private String currencyPair;
    private float usableBalancePercent;
    private float buyAtPrice = 0;
    private float buyOnPercent;
    private float sellAtPrice = 0;
    private float sellOnPercent;
    private boolean buyable;
    private boolean sellable;

    @Id
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
}
