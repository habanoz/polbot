package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
    private PoloniexPublicApi poloniexPublicApi;

    private CurrencyConfig currentCurrencyConfig = new CurrencyConfig();

    @RequestMapping({"/", "/index"})
    public String welcome(Map<String, Object> model) {

        int userId=1;  //Authenticated User
        model.put("poloniexTicker", new PoloniexTicker());
        model.put("poloniexTickerS", this.poloniexPublicApi.returnTicker());
        model.put("searchKey", "");
        return "index";
    }

    @RequestMapping(value="/setSearch", method= RequestMethod.POST)
    public String searchPost(@RequestParam("search") String search, ModelMap model) {

        int userId=1;  //Authenticated User
        Map<String,PoloniexTicker> currencyConfigs =this.poloniexPublicApi.returnTicker();

        if(search != null && !search.trim().isEmpty()){
            currencyConfigs =currencyConfigs.entrySet().stream()
                    .filter(map -> search.equalsIgnoreCase(map.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        model.put("currencyConfig", new CurrencyConfig());
        model.put("currencyConfigs", currencyConfigs);
        model.put("searchKey", search);
        return "index";
    }

}
