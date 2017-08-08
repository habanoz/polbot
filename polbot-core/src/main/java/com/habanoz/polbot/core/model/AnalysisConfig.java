package com.habanoz.polbot.core.model;

import com.habanoz.polbot.core.entity.BotUser;

import javax.persistence.*;

/**
 * Created by habanoz on 05.04.2017.
 */
public class AnalysisConfig {
    private String currencyPair;
    private float buyAtPrice = 0;
    private float buyOnPercent;
    private float sellAtPrice = 0;
    private float sellOnPercent;
    private float orderTimeoutInHour = 0;
    private int startDaysAgo;
    private long periodInSec;
    private String botName;

    public AnalysisConfig() {
    }

    public AnalysisConfig(String currencyPair, float buyAtPrice, float buyOnPercent, float sellAtPrice, float sellOnPercent, int startDaysAgo, long periodInSec,String botName) {
        this.currencyPair = currencyPair;
        this.buyAtPrice = buyAtPrice;
        this.buyOnPercent = buyOnPercent;
        this.sellAtPrice = sellAtPrice;
        this.sellOnPercent = sellOnPercent;
        this.startDaysAgo = startDaysAgo;
        this.periodInSec = periodInSec;
        this.botName = botName;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
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

    public float getOrderTimeoutInHour() {
        return orderTimeoutInHour;
    }

    public void setOrderTimeoutInHour(float orderTimeoutInHour) {
        this.orderTimeoutInHour = orderTimeoutInHour;
    }

    public int getStartDaysAgo() {
        return startDaysAgo;
    }

    public void setStartDaysAgo(int startDaysAgo) {
        this.startDaysAgo = startDaysAgo;
    }

    public long getPeriodInSec() {
        return periodInSec;
    }

    public void setPeriodInSec(long periodInSec) {
        this.periodInSec = periodInSec;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }
}
