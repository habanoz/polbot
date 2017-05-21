package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.entity.User;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;

/**
 * Created by huseyina on 4/9/2017.
 */
@Controller
public class MyCurrenciesController {

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping({"/mycurrencies/{buid}"})
    public String welcome(@PathVariable("buid") Integer buid, Map<String, Object> model, Principal principal) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        model.put("botUser", botUser);
        model.put("buid", buid);
        model.put("currencyConfig", new CurrencyConfig());
        model.put("currencyConfigs", this.currencyConfigRepository.findByBotUser(botUser));

        return "mycurrencies";
    }

    @RequestMapping(value = "/currencyconfig", params = {"save"})
    public String showCurrencyConfig(CurrencyConfig currencyConfig, Principal principal, Map model, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "currencyconfig";
        }

        this.currencyConfigRepository.save(currencyConfig);

        model.clear();

        //return "redirect:/currencyconfig?show=&currency=" + currencyConfig.getCurrencyPair();
        return "redirect:/mycurrencies/" + currencyConfig.getBotUser().getBuId();
    }

    @RequestMapping(value = "/currencyconfig", params = {"delete"})
    public String deleteCurrencyConfig(final CurrencyConfig currencyConfig, final BindingResult bindingResult, final ModelMap model) {

        if (bindingResult.hasErrors()) {
            return "currencyconfig";
        }

        this.currencyConfigRepository.delete(currencyConfig);
        model.clear();

        return "redirect:/mycurrencies/" + currencyConfig.getBotUser().getBuId();
    }

    @RequestMapping(value = "/currencyconfig/{buid}", params = {"show"})
    public String showCurrencyConfig(@RequestParam("currency") String currency, @PathVariable("buid") Integer buid, Principal principal, Map model) {

        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        CurrencyConfig currentCurrencyConfig = null;

        if (botUser != null)
            currentCurrencyConfig = this.currencyConfigRepository.findByBotUserAndCurrencyPair(botUser, currency);

        if (currentCurrencyConfig == null)
            currentCurrencyConfig = new CurrencyConfig();

        if (currentCurrencyConfig.getBotUser() == null)
            currentCurrencyConfig.setBotUser(botUser);

        model.put("botUser", botUser);
        model.put("currency", currency);
        model.put("currencyConfig", currentCurrencyConfig);

        return "currencyconfig";
    }

}
