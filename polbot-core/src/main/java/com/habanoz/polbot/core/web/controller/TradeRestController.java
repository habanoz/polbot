package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.TradeHistoryRecord;
import com.habanoz.polbot.core.repository.TradeHistoryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 01.07.2017.
 */
@RestController
public class TradeRestController {
    @Autowired
    private TradeHistoryRecordRepository tradeHistoryRecordRepository;

    @RequestMapping({"/gettradehistory/{currPair}/{period}"})
    public List<TradeHistoryRecord> getTradeHistory(@PathVariable String currPair, @PathVariable String period, Map<String, Object> model, Principal principal) {
        int multiplier = 1;
        switch (period.toLowerCase()) {
            case "15mm":
                multiplier = 1;
                break;
            case "30mm":
                multiplier = 2;
                break;
            case "hh":
                multiplier = 4;
                break;
            case "1d":
                multiplier = 96;
                break;
            case "1w":
                multiplier = 96 * 7;
                break;
            case "1m":
                multiplier = 96 * 7 * 4;
                break;
            default:
                multiplier = 96;
        }
        if (multiplier == 1)
            return tradeHistoryRecordRepository.findByCurrencyPair(currPair);
        else {
            List<TradeHistoryRecord> records = new ArrayList<>();
            List<TradeHistoryRecord> tradeHistoryRecords = tradeHistoryRecordRepository.findByCurrencyPair(currPair);
            for (int idx = 0; idx < tradeHistoryRecords.size(); idx += multiplier) {
                TradeHistoryRecord tradeHistoryAggregate = new TradeHistoryRecord(tradeHistoryRecords.get(idx));
                tradeHistoryAggregate.setClose(tradeHistoryRecords.get(tradeHistoryRecords.size() - 1).getClose());
                for (int bubIdx = 1; bubIdx < multiplier && idx + bubIdx < tradeHistoryRecords.size(); bubIdx++) {
                    TradeHistoryRecord tradeHistoryRecord = tradeHistoryRecords.get(idx + bubIdx);
                    tradeHistoryAggregate.setBuyVol(tradeHistoryAggregate.getBuyVol() + tradeHistoryRecord.getBuyVol());
                    tradeHistoryAggregate.setSellVol(tradeHistoryAggregate.getSellVol() + tradeHistoryRecord.getSellVol());

                    tradeHistoryAggregate.setBuyQVol(tradeHistoryAggregate.getBuyQVol() + tradeHistoryRecord.getBuyQVol());
                    tradeHistoryAggregate.setSellQVol(tradeHistoryAggregate.getSellQVol() + tradeHistoryRecord.getSellQVol());

                    if (tradeHistoryRecord.getHigh().doubleValue() > tradeHistoryAggregate.getHigh().doubleValue())
                        tradeHistoryAggregate.setHigh(tradeHistoryRecord.getHigh());

                    if (tradeHistoryRecord.getLow().doubleValue() < tradeHistoryAggregate.getLow().doubleValue())
                        tradeHistoryAggregate.setLow(tradeHistoryRecord.getLow());
                }

                records.add(tradeHistoryAggregate);
            }

            return records;
        }
    }
}
