package com.habanoz.polbot.core.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by habanoz on 22.04.2017.
 */
@Entity
public class UserBot implements Serializable {
    private Integer id;
    private BotUser user;
    private Bot bot;
    private boolean active;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JoinColumn(unique = true)
    @OneToOne
    public BotUser getUser() {
        return user;
    }

    public void setUser(BotUser user) {
        this.user = user;
    }

    @ManyToOne
    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserBot userBot = (UserBot) o;

        if (active != userBot.active) return false;
        if (id != null ? !id.equals(userBot.id) : userBot.id != null) return false;
        if (user != null ? !user.equals(userBot.user) : userBot.user != null) return false;
        return bot != null ? bot.equals(userBot.bot) : userBot.bot == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (bot != null ? bot.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }
}
