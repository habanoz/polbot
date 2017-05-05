package com.habanoz.polbot.core.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Yuce on 4/9/2017.
 */
@Entity
public class BotUser implements Serializable{
    private int userId;
    private String publicKey;
    private String privateKey;
    private boolean active;
    private String userEmail;
    private String userName;
    private String password;
    private String role;

    private boolean emailNotification;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "BotUser{" +
                "userName='" + userName + '\'' +
                '}';
    }


    public boolean isEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }
}
