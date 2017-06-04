package com.habanoz.polbot.core.utils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by huseyina on 6/4/2017.
 */
public class ExchangePrice {
    private BigDecimal buyPrice;
    private Date date;
    private BigDecimal volume;
    private BigDecimal sellPrice;

    public ExchangePrice(BigDecimal buyPrice, BigDecimal sellPrice, Date date, BigDecimal volume) {
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.date = date;
        this.volume = volume;
    }

    public ExchangePrice(BigDecimal buyPrice, Date date, BigDecimal volume) {
        this(buyPrice, buyPrice, date, volume);
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public Date getDate() {
        return date;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }
}
