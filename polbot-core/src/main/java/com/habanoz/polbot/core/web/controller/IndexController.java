package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        int userId=1;  //Authenticated User
        model.put("currencyConfig", new CurrencyConfig());
        model.put("currencyConfigs", this.currencyConfigRepository.findByUserId(userId));
        model.put("searchKey", "");
        return "index";
    }
    @RequestMapping(value="/setSearch", method= RequestMethod.POST)
    public String searchPost(@RequestParam("search") String search, ModelMap model) {

        int userId=1;  //Authenticated User
        List<CurrencyConfig> currencyConfigs =this.currencyConfigRepository.findByUserId(userId);

        if(search != null && !search.trim().isEmpty()){
            currencyConfigs =currencyConfigs.stream().filter(r->r.getCurrencyPair().toLowerCase().indexOf(search.toLowerCase()) > 0).collect(Collectors.toList());
        }

        model.put("currencyConfig", new CurrencyConfig());
        model.put("currencyConfigs", currencyConfigs);
        model.put("searchKey", search);
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
