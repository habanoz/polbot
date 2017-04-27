package com.habanoz.polbot.core.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Yuce on 4/27/2017.
 */
@Entity
public class CurrenyOrder implements Serializable {
    private Integer currenyOrderId;
    private String orderNumber;
    private String currencyPair;
    private String orderType;
    private Integer userId;
    private LocalDateTime orderDate;
    private String tradeID;


    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Integer getCurrenyOrderId() {
        return currenyOrderId;
    }

    public void setCurrenyOrderId(Integer currenyOrderId) {
        this.currenyOrderId = currenyOrderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getTradeID() {
        return tradeID;
    }

    public void setTradeID(String tradeID) {
        this.tradeID = tradeID;
    }
}
