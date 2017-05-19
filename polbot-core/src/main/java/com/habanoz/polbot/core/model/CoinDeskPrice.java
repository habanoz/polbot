package com.habanoz.polbot.core.model;

/**
 * Created by huseyina on 5/16/2017.
 */
public class CoinDeskPrice {
    private String code;
    private String rate;
    private String description;
    private float rate_float;



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getRate_float() {
        return rate_float;
    }

    public void setRate_float(float rate_float) {
        this.rate_float = rate_float;
    }
}
