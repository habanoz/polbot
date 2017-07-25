package com.habanoz.polbot.core.config;

import com.habanoz.polbot.core.entity.User;
import com.habanoz.polbot.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Date;

/**
 * Created by habanoz on 25.07.2017.
 */
@Service
public class LoginSucessHandler extends
        SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        // record login success of user
        super.onAuthenticationSuccess(request, response, authentication);

        User loginUser = userRepository.findByUserName(user.getUsername());
        loginUser.setLastLogin(new Date());
    }

}