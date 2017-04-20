package com.habanoz.polbot.core.service;

import org.springframework.security.core.Authentication;

/**
 * Created by Yuce on 4/20/2017.
 */
public interface IAuthenticationFacade {
    Authentication getAuthentication();
    int GetUserId();
}