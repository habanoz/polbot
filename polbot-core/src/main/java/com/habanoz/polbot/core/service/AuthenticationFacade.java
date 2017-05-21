package com.habanoz.polbot.core.service;

import com.habanoz.polbot.core.entity.User;
import com.habanoz.polbot.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Created by Yuce on 4/20/2017.
 */
@Component
public class AuthenticationFacade implements IAuthenticationFacade {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public int GetUserId() {
        Authentication authentication = getAuthentication();
        String userName =  authentication.getName();

       // BotUser botUser2 =  (BotUser) authentication.getPrincipal();
        User user =  userRepository.findByUserName(userName);
        return user.getId();
    }


}