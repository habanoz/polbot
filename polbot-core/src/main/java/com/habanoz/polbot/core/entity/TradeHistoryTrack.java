package com.habanoz.polbot.core.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by huseyina on 4/19/2017.
 */
@Entity
public class TradeHistoryTrack {
    private Integer userId;
    private Long lastTimeStamp;

    @Id
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public void setLastTimeStamp(Long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }
}
