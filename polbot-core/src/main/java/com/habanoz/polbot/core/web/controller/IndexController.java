package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by huseyina on 4/9/2017.
 */
@Controller
public class IndexController {


    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    private CurrencyConfig currentCurrencyConfig = new CurrencyConfig();

    @RequestMapping({"/", "/index"})
    public String welcome(Map<String, Object> model) {

        model.put("currencyConfig", new CurrencyConfig());
        model.put("currencyConfigs", this.currencyConfigRepository.findAll());
        return "index";
    }


    @RequestMapping(value = "/currencyconfig", params = {"save"})
    public String saveCurrencyConfig(final CurrencyConfig currencyConfig, final BindingResult bindingResult, final ModelMap model) {

        if (bindingResult.hasErrors()) {
            return "currencyconfig";
        }

        this.currencyConfigRepository.save(currencyConfig);
        model.clear();

        return "redirect:/currencyconfig?show=&currency=" + currencyConfig.getCurrencyPair();
    }

    @RequestMapping(value = "/currencyconfig", params = {"delete"})
    public String deleteCurrencyConfig(final CurrencyConfig currencyConfig, final BindingResult bindingResult, final ModelMap model) {

        if (bindingResult.hasErrors()) {
            return "currencyconfig";
        }

        this.currencyConfigRepository.delete(currencyConfig);
        model.clear();

        return "redirect:/index";
    }

    @RequestMapping(value = "/currencyconfig", params = {"show"})
    public String showCurrencyConfig(@RequestParam("currency") String currency, Map model) {
        currentCurrencyConfig = this.currencyConfigRepository.findOne(currency);

        if (currentCurrencyConfig == null)
            currentCurrencyConfig = new CurrencyConfig();

        model.put("currency", currency);
        model.put("currencyConfig", currentCurrencyConfig);
        return "/currencyconfig";
    }

    @RequestMapping(value = "/currencyconfig")
    public String getCurrency(final CurrencyConfig currencyConfig, final BindingResult bindingResult, final ModelMap model) {

        if (bindingResult.hasErrors()) {
            return "curr";
        }

        return "currencyconfig";
    }

}
