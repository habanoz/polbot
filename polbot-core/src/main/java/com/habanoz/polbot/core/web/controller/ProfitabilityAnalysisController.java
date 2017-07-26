package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexPublicApiImpl;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.robot.CascadedPatienceStrategy;
import com.habanoz.polbot.core.robot.PatienceStrategy;
import com.habanoz.polbot.core.robot.PolBot;
import com.habanoz.polbot.core.robot.PolStrategy;
import com.habanoz.polbot.core.service.ProfitAnalysisService;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by huseyina on 5/29/2017.
 */
@Controller
public class ProfitabilityAnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(ProfitabilityAnalysisController.class);

    @Autowired
    private ProfitAnalysisService profitAnalysisService;


    @RequestMapping(value = "/analyse", params = {"analyse"})
    public String analyse(AnalysisConfig analysisConfig, Map model) {

        Map<String, Object> resultMap = profitAnalysisService.execute(analysisConfig, 1.0f);

        model.put("result", resultMap);

        model.put("analysisConfig", analysisConfig);

        return "profitanalysis";
    }

    @RequestMapping(value = "/analyse")
    public String analyse(Map model) {
        AnalysisConfig analysisConfig = new AnalysisConfig("BTC_DGB", 0, 10, 0, 10,30, 300L);
        model.put("analysisConfig", analysisConfig);

        return "profitanalysis";
    }
}
