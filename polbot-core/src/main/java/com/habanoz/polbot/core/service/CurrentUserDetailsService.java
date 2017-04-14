package com.habanoz.polbot.core.service;

import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.repository.BotUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Created by habanoz on 14.04.2017.
 */
@Service
public class CurrentUserDetailsService implements UserDetailsService {
    @Autowired
    private BotUserRepository botUserRepository;

    @Override
    public CurrentUser loadUserByUsername(String email)  {
        BotUser user = botUserRepository.findByUserEmail(email);
        return new CurrentUser(user);
    }
}