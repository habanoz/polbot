package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
/**
 * Created by Yuce on 4/11/2017.
 */
@Controller
public class UsersController {

    @Autowired
    private BotUserRepository botUserRepository;

    @RequestMapping(value = "/botusers/allusers")
    public String welcome(Map<String, Object> model) {

        model.put("botUsers", this.botUserRepository.findAll());

        return "allusers";
    }
}
