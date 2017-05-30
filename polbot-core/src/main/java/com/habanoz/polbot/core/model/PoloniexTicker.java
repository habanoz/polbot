package com.habanoz.polbot.core.model;

import com.google.gson.Gson;

import java.math.BigDecimal;

public class PoloniexTicker
{
    private BigDecimal id;
    private BigDecimal last;
    private BigDecimal lowestAsk;
    private BigDecimal highestBid;
    private BigDecimal percentChange;
    private BigDecimal baseVolume;
    private BigDecimal quoteVolume;
    private BigDecimal isFrozen;
    private BigDecimal high24hr;
    private BigDecimal low24hr;

    public PoloniexTicker() {
    }

    public PoloniexTicker(BigDecimal last, BigDecimal lowestAsk, BigDecimal highestBid, BigDecimal percentChange, BigDecimal baseVolume) {
        this.last = last;
        this.lowestAsk = lowestAsk;
        this.highestBid = highestBid;
        this.percentChange = percentChange;
        this.baseVolume = baseVolume;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public BigDecimal getLast() {
        return last;
    }

    public void setLast(BigDecimal last) {
        this.last = last;
    }

    public BigDecimal getLowestAsk() {
        return lowestAsk;
    }

    public void setLowestAsk(BigDecimal lowestAsk) {
        this.lowestAsk = lowestAsk;
    }

    public BigDecimal getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(BigDecimal highestBid) {
        this.highestBid = highestBid;
    }

    public BigDecimal getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(BigDecimal percentChange) {
        this.percentChange = percentChange;
    }

    public BigDecimal getBaseVolume() {
        return baseVolume;
    }

    public void setBaseVolume(BigDecimal baseVolume) {
        this.baseVolume = baseVolume;
    }

    public BigDecimal getQuoteVolume() {
        return quoteVolume;
    }

    public void setQuoteVolume(BigDecimal quoteVolume) {
        this.quoteVolume = quoteVolume;
    }

    public BigDecimal getIsFrozen() {
        return isFrozen;
    }

    public void setIsFrozen(BigDecimal isFrozen) {
        this.isFrozen = isFrozen;
    }

    public BigDecimal getHigh24hr() {
        return high24hr;
    }

    public void setHigh24hr(BigDecimal high24hr) {
        this.high24hr = high24hr;
    }

    public BigDecimal getLow24hr() {
        return low24hr;
    }

    public void setLow24hr(BigDecimal low24hr) {
        this.low24hr = low24hr;
    }

    @Override
    public String toString()
    {
        return new Gson().toJson(this);
    }
}
