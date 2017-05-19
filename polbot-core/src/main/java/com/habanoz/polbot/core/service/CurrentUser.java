package com.habanoz.polbot.core.service;

import com.habanoz.polbot.core.entity.BotUser;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Created by habanoz on 14.04.2017.
 */
public class CurrentUser extends org.springframework.security.core.userdetails.User {

    private BotUser user;

    public CurrentUser(BotUser user) {
        super(user.getUserEmail(), user.getPassword(), AuthorityUtils.createAuthorityList());
        this.user = user;
    }

    public BotUser getUser() {
        return user;
    }

    public Integer getId() {
        return user.getUserId();
    }
}