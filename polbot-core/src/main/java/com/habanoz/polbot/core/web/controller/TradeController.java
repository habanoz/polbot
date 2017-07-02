package com.habanoz.polbot.core.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Created by habanoz on 01.07.2017.
 */
@Controller
public class TradeController {
    @RequestMapping({"/tradehistory/{currencyPair}/{period}"})
    public String welcome(Map<String, Object> model, @PathVariable String currencyPair, @PathVariable String period) {
        model.put("period", period);
        model.put("currPair", currencyPair);
        return "tradehistory";
    }
}
