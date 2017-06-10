package com.habanoz.polbot.core.job;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.entity.TradeHistoryRecord;
import com.habanoz.polbot.core.repository.TradeHistoryRecordRepository;
import com.habanoz.polbot.core.robot.PolBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by huseyina on 6/10/2017.
 */
@Component
public class TradeHistory {
    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private TradeHistoryRecordRepository tradeHistoryRecordRepository;

    @PostConstruct
    public void init() {
    }

    @Scheduled(cron = "0 0 * * * *")
    public void execute() {
        long end = System.currentTimeMillis() / 1000;
        long start = end - 60 * 60;

        Map<String, List<PoloniexTrade>> tradeHistory = publicApi.returnTradeHistory("all", start, end);
        for (Map.Entry<String, List<PoloniexTrade>> tradeEntry : tradeHistory.entrySet()) {

            List<PoloniexTrade> list = tradeEntry.getValue();
            BigDecimal buy = BigDecimal.valueOf(0);
            BigDecimal sell = BigDecimal.valueOf(0);
            for (PoloniexTrade trade : list) {
                if (trade.getType().equalsIgnoreCase(PolBot.BUY_ACTION))
                    buy = buy.add(trade.getTotal());
                else
                    sell = sell.add(trade.getTotal());

            }

            TradeHistoryRecord tradeHistoryRecord = new TradeHistoryRecord(tradeEntry.getKey(), start, buy.doubleValue(), sell.doubleValue());
            tradeHistoryRecordRepository.save(tradeHistoryRecord);
        }
    }
}
