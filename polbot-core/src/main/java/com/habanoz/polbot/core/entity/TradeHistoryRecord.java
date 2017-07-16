package com.habanoz.polbot.core.entity;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by huseyina on 6/10/2017.
 */
@Entity
public class TradeHistoryRecord {
    private Integer id;
    private String currencyPair;
    private Long start;
    private Double buyVol;
    private Double sellVol;
    private Double buyQVol;
    private Double sellQVol;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;

    public TradeHistoryRecord() {
    }

    public TradeHistoryRecord(TradeHistoryRecord tradeHistoryRecord) {
        this.currencyPair = tradeHistoryRecord.getCurrencyPair();
        this.buyVol = tradeHistoryRecord.buyVol;
        this.buyQVol = tradeHistoryRecord.buyQVol;
        this.sellVol = tradeHistoryRecord.sellVol;
        this.sellQVol = tradeHistoryRecord.sellQVol;
        this.start = tradeHistoryRecord.start;
        this.open = tradeHistoryRecord.open;
        this.close = tradeHistoryRecord.close;
        this.high = tradeHistoryRecord.high;
        this.low = tradeHistoryRecord.low;
    }

    public TradeHistoryRecord(String currencyPair, Long start, Double buyVol, Double sellVol,Double buyQVol, Double sellQVol) {
        this.currencyPair = currencyPair;
        this.start = start;
        this.buyVol = buyVol;
        this.sellVol = sellVol;
        this.buyQVol = buyQVol;
        this.sellQVol = sellQVol;
    }

    public TradeHistoryRecord(String currencyPair, Long start, Double buyVol, Double sellVol,Double buyQVol, Double sellQVol, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low) {
        this.currencyPair = currencyPair;
        this.start = start;
        this.buyVol = buyVol;
        this.sellVol = sellVol;
        this.buyQVol = buyQVol;
        this.sellQVol = sellQVol;
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

    public Double getBuyVol() {
        return buyVol;
    }

    public void setBuyVol(Double buyVol) {
        this.buyVol = buyVol;
    }

    public Double getSellVol() {
        return sellVol;
    }

    public void setSellVol(Double sellVol) {
        this.sellVol = sellVol;
    }

    public Double getBuyQVol() {
        return buyQVol;
    }

    public void setBuyQVol(Double buyQVol) {
        this.buyQVol = buyQVol;
    }

    public Double getSellQVol() {
        return sellQVol;
    }

    public void setSellQVol(Double sellQVol) {
        this.sellQVol = sellQVol;
    }

    @Column(precision = 12, scale = 9)
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
                ", buy vol=" + buyVol +
                ", sell vol=" + sellVol +
                '}';
    }
}
