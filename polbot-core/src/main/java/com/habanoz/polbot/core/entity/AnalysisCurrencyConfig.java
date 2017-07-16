package com.habanoz.polbot.core.entity;

import javax.persistence.*;

/**
 * Created by habanoz on 05.04.2017.
 */
@Entity
public class AnalysisCurrencyConfig {
    private String currencyPair;
    private Boolean enabled;

    public AnalysisCurrencyConfig() {
    }

    @Id
    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
