package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.TradeHistoryRecord;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.registry.PublicPoloniexTickerRegistry;
import com.habanoz.polbot.core.repository.TradeHistoryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by habanoz on 01.07.2017.
 */
@Controller
public class TradeController {
    @RequestMapping({"/tradehistory"})
    public String welcome(Map<String, Object> model) {

        return "tradehistory";
    }
}
