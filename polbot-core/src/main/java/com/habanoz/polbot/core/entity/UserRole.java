package com.habanoz.polbot.core.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class UserRole implements Serializable{
    private String user_email;
    private String role;

    @Id
    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    @Id
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
