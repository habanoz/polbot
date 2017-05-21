package com.habanoz.polbot.core.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Yuce on 4/9/2017.
 */
@Entity
public class Bot implements Serializable {
    private Integer id;
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
        return id;
    }

    public void setId(Integer userId) {
        this.id = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bot bot = (Bot) o;

        if (id != null ? !id.equals(bot.id) : bot.id != null) return false;
        return name != null ? name.equals(bot.name) : bot.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
