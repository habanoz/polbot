package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.registry.PublicPoloniexTickerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huseyina on 4/9/2017.
 */
@Controller
public class IndexController {

    @Autowired
    private PublicPoloniexTickerRegistry publicRegistry;

    @RequestMapping({"/", "/index"})
    public String welcome(Map<String, Object> model) {
        //int userId = authenticationFacade.GetUserId();  //Authenticated User
        PublicPoloniexTickerRegistry.TickerPack tickerPack = publicRegistry.getTickerMap();
        Map<String, PoloniexTicker> map = tickerPack.getTickerMap().entrySet().stream().filter(e -> e.getKey().startsWith("BTC")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        model.put("poloniexTicker", new PoloniexTicker());
        model.put("poloniexTickerPack", new PublicPoloniexTickerRegistry.TickerPack(tickerPack.getLastTickerMapDate(),map));
        model.put("searchKey", "");
        return "index";
    }

}
