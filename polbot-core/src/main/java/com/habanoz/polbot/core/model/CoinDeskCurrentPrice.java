package com.habanoz.polbot.core.model;

import java.util.Map;

/**
 * Created by huseyina on 5/16/2017.
 */
public class CoinDeskCurrentPrice {
    private Map<String,String> time;
    private String disclaimer;
    private Map<String,CoinDeskPrice> bpi;

    public Map<String, CoinDeskPrice> getBpi() {
        return bpi;
    }

    public void setBpi(Map<String, CoinDeskPrice> bpi) {
        this.bpi = bpi;
    }

    public Map<String, String> getTime() {
        return time;
    }

    public void setTime(Map<String, String> time) {
        this.time = time;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
