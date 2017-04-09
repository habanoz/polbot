package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Created by huseyina on 4/9/2017.
 */
@Controller
public class IndexController {

    @Value("${config.currency.page.title}")
    private String pageTitle;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    private CurrencyConfig currentCurrencyConfig = new CurrencyConfig();

    @RequestMapping("/")
    public String welcome(Map<String, Object> model) {
        model.put("pageTitle", this.pageTitle);

        model.put("currencyConfig", new CurrencyConfig());
        model.put("currencyConfigs", this.currencyConfigRepository.findAll());
        return "index";
    }


    @RequestMapping(value = "/currencyconfig", params = {"save"})
    public String saveSeedstarter(final CurrencyConfig currencyConfig, final BindingResult bindingResult, final ModelMap model) {

        if (bindingResult.hasErrors()) {
            return "seedstartermng";
        }

        this.currencyConfigRepository.save(currencyConfig);
        model.clear();

        return "redirect:/currencyconfig";
    }

    @RequestMapping(value = "/currencyconfig", params = {"show"})
    public String showCurrencyConfig(@RequestParam("currency") String currency, Map model) {
        currentCurrencyConfig = this.currencyConfigRepository.findOne(currency);
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
