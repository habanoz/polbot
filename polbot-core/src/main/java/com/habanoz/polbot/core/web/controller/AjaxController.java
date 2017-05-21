package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Yuce on 4/11/2017.
 */
@Controller

public class AjaxController {

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;


    @RequestMapping(value = "/GetAjaxUsers", method = RequestMethod.GET)
    @ResponseBody
    public String GetUserCurrencies(@RequestParam("botUserId") int botUserId) throws IOException {
        BotUser botUser = botUserRepository.findOne(botUserId);

        // int userId=1;
        List<CurrencyConfig> userCurrencyConfigs = currencyConfigRepository.findByBotUser(botUser);
        List<CurrencyConfig> currencyConfigs = userCurrencyConfigs.stream().filter(r -> r.getBuyable() || r.getSellable()).collect(Collectors.toList());

        String currencyConfigsJson = new ObjectMapper().writeValueAsString(currencyConfigs);
        Map<String, String> payload = new HashMap<>();
        payload.put("userCurrencies", currencyConfigsJson);

        return new ObjectMapper().writeValueAsString(payload);
    }

}
