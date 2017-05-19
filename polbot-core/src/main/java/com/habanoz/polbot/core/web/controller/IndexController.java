package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.registry.PublicRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Created by huseyina on 4/9/2017.
 */
@Controller
public class IndexController {

    @Autowired
    private PublicRegistry publicRegistry;

    @RequestMapping({"/", "/index"})
    public String welcome(Map<String, Object> model) {
        //int userId = authenticationFacade.GetUserId();  //Authenticated User

        model.put("poloniexTicker", new PoloniexTicker());
        model.put("poloniexTickerPack",publicRegistry.getTickerMap() );
        model.put("searchKey", "");
        return "index";
    }

}
