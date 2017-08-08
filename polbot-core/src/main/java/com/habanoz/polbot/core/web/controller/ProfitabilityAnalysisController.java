package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexPublicApiImpl;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.repository.BotRepository;
import com.habanoz.polbot.core.robot.*;
import com.habanoz.polbot.core.service.ProfitAnalysisService;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Created by huseyina on 5/29/2017.
 */
@Controller
public class ProfitabilityAnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(ProfitabilityAnalysisController.class);

    @Autowired
    private ProfitAnalysisService profitAnalysisService;
    @Autowired
    private BotRepository botRepository;


    @RequestMapping(value = "/analyse", params = {"analyse"})
    public String analyse(AnalysisConfig analysisConfig, Map model) {

        Map<String, Object> resultMap = profitAnalysisService.execute(analysisConfig, 1.0f);
        List<PoloniexTrade> historyList = (List<PoloniexTrade>) resultMap.get("history");
        List<PoloniexTrade> buys = new ArrayList<>();
        List<PoloniexTrade> sells = new ArrayList<>();
        for (PoloniexTrade poloniexTrade : historyList)
            if (poloniexTrade.getType().equalsIgnoreCase(PolBot.BUY_ACTION))
                buys.add(poloniexTrade);
            else sells.add(poloniexTrade);

        StrBuilder sbBuys=new StrBuilder("[");
        for (PoloniexTrade trade:buys)
            sbBuys.append("{date:").append(trade.getDate().getTime()).append(",value:").append(trade.getRate().doubleValue()).append("},");
        sbBuys.append("]");

        StrBuilder sbSels=new StrBuilder("[");
        for (PoloniexTrade trade:sells)
            sbSels.append("{date:").append(trade.getDate().getTime()).append(",value:").append(trade.getRate().doubleValue()).append("},");
        sbSels.append("]");

        model.put("result", resultMap);
        model.put("buys", sbBuys.toString());
        model.put("sells", sbSels.toString());
        model.put("bots", botRepository.findAll());
        model.put("analysisConfig", analysisConfig);

        return "profitanalysis";
    }

    @RequestMapping(value = "/analyse")
    public String analyse(Map model) {
        AnalysisConfig analysisConfig = new AnalysisConfig("BTC_DGB", 0, 10, 0, 10, 30, 300L, PoloniexPatienceStrategyBot.class.getSimpleName());
        model.put("analysisConfig", analysisConfig);
        model.put("bots", botRepository.findAll());

        return "profitanalysis";
    }
}
