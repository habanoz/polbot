package com.habanoz.polbot.core.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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


    public TradeHistoryRecord(String currencyPair, Long start, Double buy, Double sell) {
        this.currencyPair = currencyPair;
        this.start = start;
        this.buy = buy;
        this.sell = sell;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TradeHistoryRecord that = (TradeHistoryRecord) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (currencyPair != null ? !currencyPair.equals(that.currencyPair) : that.currencyPair != null) return false;
        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        if (buy != null ? !buy.equals(that.buy) : that.buy != null) return false;
        return sell != null ? sell.equals(that.sell) : that.sell == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (currencyPair != null ? currencyPair.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (buy != null ? buy.hashCode() : 0);
        result = 31 * result + (sell != null ? sell.hashCode() : 0);
        return result;
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
