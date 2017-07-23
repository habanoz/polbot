package com.habanoz.polbot.core.entity;

import javax.persistence.*;

/**
 * Created by habanoz on 05.04.2017.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"bot_user", "currencyPair"}))
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
    private BotUser botUser;
    private float buyOrderCancellationHour=0;

    public CurrencyConfig() {
    }

    public CurrencyConfig(String currencyPair, float usableBalancePercent, float buyAtPrice, float buyOnPercent, float sellAtPrice, float sellOnPercent) {
        this.currencyPair = currencyPair;
        this.usableBalancePercent = usableBalancePercent;
        this.buyAtPrice = buyAtPrice;
        this.buyOnPercent = buyOnPercent;
        this.sellAtPrice = sellAtPrice;
        this.sellOnPercent = sellOnPercent;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getCurrencyConfigId() {
        return currencyConfigId;
    }

    public void setCurrencyConfigId(Integer currencyConfigId) {
        this.currencyConfigId = currencyConfigId;
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

    public float getBuyOrderCancellationHour() {
        return buyOrderCancellationHour;
    }

    public void setBuyOrderCancellationHour(float buyOrderCancellationHour) {
        this.buyOrderCancellationHour = buyOrderCancellationHour;
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
        if (botUser != null ? !botUser.equals(that.botUser) : that.botUser != null) return false;
        return Float.compare(that.buyOrderCancellationHour, buyOrderCancellationHour) != 0;
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
        result = 31 * result + (botUser != null ? botUser.hashCode() : 0);
        result = 31 * result + (buyOrderCancellationHour !=  +0.0f ? Float.floatToIntBits(buyOrderCancellationHour) : 0);
        return result;
    }
}
