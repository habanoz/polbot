package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.TradeHistoryRecord;
import com.habanoz.polbot.core.repository.TradeHistoryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 01.07.2017.
 */
@RestController
public class TradeRestController {
    @Autowired
    private TradeHistoryRecordRepository tradeHistoryRecordRepository;

    @RequestMapping({"/tradehistory/{currPair}"})
    public List<TradeHistoryRecord> getTradeHistory(@PathVariable String currPair, Map<String, Object> model, Principal principal) {
        return tradeHistoryRecordRepository.findByCurrencyPair(currPair);
    }
}
