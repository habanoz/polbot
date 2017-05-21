package com.habanoz.polbot.core.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Yuce on 4/9/2017.
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "buid"})
})
public class BotUser implements Serializable {
    private Integer id;
    private User user;
    private Integer buId;
    private String publicKey;
    private String privateKey;
    private String userEmail;
    private boolean emailNotification = false;
    private boolean active;
    private String description;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
        return id;
    }

    public void setId(Integer userId) {
        this.id = userId;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getBuId() {
        return buId;
    }

    public void setBuId(Integer buId) {
        this.buId = buId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BotUser botUser = (BotUser) o;

        if (emailNotification != botUser.emailNotification) return false;
        if (active != botUser.active) return false;
        if (id != null ? !id.equals(botUser.id) : botUser.id != null) return false;
        if (user != null ? !user.equals(botUser.user) : botUser.user != null) return false;
        if (buId != null ? !buId.equals(botUser.buId) : botUser.buId!= null) return false;
        if (publicKey != null ? !publicKey.equals(botUser.publicKey) : botUser.publicKey != null) return false;
        if (privateKey != null ? !privateKey.equals(botUser.privateKey) : botUser.privateKey != null) return false;
        return userEmail != null ? userEmail.equals(botUser.userEmail) : botUser.userEmail == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (buId != null ? buId.hashCode() : 0);
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        result = 31 * result + (privateKey != null ? privateKey.hashCode() : 0);
        result = 31 * result + (userEmail != null ? userEmail.hashCode() : 0);
        result = 31 * result + (emailNotification ? 1 : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }
}
