package com.habanoz.polbot.core.entity;

import com.habanoz.polbot.core.model.PoloniexOpenOrder;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by huseyina on 4/7/2017.
 */
@Entity
public class OrderEntity {
    private Integer id;
    private PoloniexOpenOrder order;
    private Date dateCreated;
    private Date dateClosed;
    private String status;

    public OrderEntity() {
    }

    public OrderEntity(PoloniexOpenOrder order, Date dateCreated) {
        this.order = order;
        this.dateCreated = dateCreated;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Embedded
    public PoloniexOpenOrder getOrder() {
        return order;
    }

    public void setOrder(PoloniexOpenOrder order) {
        this.order = order;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateClosed() {
        return dateClosed;
    }

    public void setDateClosed(Date dateClosed) {
        this.dateClosed = dateClosed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
