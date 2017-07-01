package com.habanoz.polbot.core.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * Created by huseyina on 6/10/2017.
 */
@Entity
public class TradeHistoryRecord {
    private Integer id;
    private String currencyPair;
    private Long start;
    private Double buy;
    private Double sell;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;

    public TradeHistoryRecord() {
    }

    public TradeHistoryRecord(String currencyPair, Long start, Double buy, Double sell) {
        this.currencyPair = currencyPair;
        this.start = start;
        this.buy = buy;
        this.sell = sell;
    }

    public TradeHistoryRecord(String currencyPair, Long start, Double buy, Double sell, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low) {
        this.currencyPair = currencyPair;
        this.start = start;
        this.buy = buy;
        this.sell = sell;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Double getBuy() {
        return buy;
    }

    public void setBuy(Double buy) {
        this.buy = buy;
    }

    public Double getSell() {
        return sell;
    }

    public void setSell(Double sell) {
        this.sell = sell;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }



    @Override
    public String toString() {
        return "TradeHistoryRecord{" +
                "id=" + id +
                ", currencyPair='" + currencyPair + '\'' +
                ", start=" + start +
                ", buy=" + buy +
                ", sell=" + sell +
                '}';
    }
}
