package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * Created by Yuce on 4/11/2017.
 */
@Controller

public class AjaxController {

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @RequestMapping(value = "/GetAjaxUsers", method = RequestMethod.POST)
    @ResponseBody
    public String GetUserCurrencies(@RequestParam("userId") int userId) {

       // int userId=1;
        List<CurrencyConfig> userCurrencies = currencyConfigRepository.findByUserId(userId);
       // Returned the render html page from over here.
        return "test";
    }


//    @RequestMapping(value = "/GetAjaxUsers", method = RequestMethod.GET)
//    @ResponseBody
//    public List<CurrencyConfig> GetUserCurrencies(@RequestParam("userId") int userId) {
//
//        // int userId=1;
//        List<CurrencyConfig> userCurrencies = currencyConfigRepository.findByUserId(userId);
//        // Returned the render html page from over here.
//        return userCurrencies;
//    }
}
