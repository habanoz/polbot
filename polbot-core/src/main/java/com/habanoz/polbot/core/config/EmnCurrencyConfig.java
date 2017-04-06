package com.habanoz.polbot.core.config;

/**
 * Created by habanoz on 05.04.2017.
 */
public class EmnCurrencyConfig {
    private String currencyName;
    private float usableBalancePercent;
    private float buyOnPercent;
    private float sellOnPercent;

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

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyName() {
        return currencyName;
    }
}
