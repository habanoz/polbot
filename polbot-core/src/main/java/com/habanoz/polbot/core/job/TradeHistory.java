package com.habanoz.polbot.core.job;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.entity.TradeHistoryRecord;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.repository.TradeHistoryRecordRepository;
import com.habanoz.polbot.core.robot.PolBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by huseyina on 6/10/2017.
 */
@Component
public class TradeHistory {
    public static final String BTC_ETC = "BTC_ETC";
    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private TradeHistoryRecordRepository tradeHistoryRecordRepository;

    @PostConstruct
    public void init() {
    }

    @Scheduled(cron = "0 0/15 * * * ?")
    public void execute() {
        long end = System.currentTimeMillis() / 1000;
        long start = end - 60 * 15;

        List<PoloniexTrade> tradeHistory = publicApi.returnTradeHistory(BTC_ETC, start, end);
        BigDecimal buy = BigDecimal.valueOf(0);
        BigDecimal sell = BigDecimal.valueOf(0);
        BigDecimal low = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal high = BigDecimal.valueOf(0);
        BigDecimal open = tradeHistory.get(0).getRate();
        BigDecimal close = tradeHistory.get(tradeHistory.size() - 1).getRate();

        for (PoloniexTrade trade : tradeHistory) {

            if (trade.getType().equalsIgnoreCase(PolBot.BUY_ACTION))
                buy = buy.add(trade.getTotal());
            else
                sell = sell.add(trade.getTotal());

            if (trade.getRate().doubleValue() > high.doubleValue())
                high = trade.getRate();

            if (trade.getRate().doubleValue() < low.doubleValue())
                low = trade.getRate();

        }

        TradeHistoryRecord tradeHistoryRecord = new TradeHistoryRecord(BTC_ETC, start, buy.doubleValue(), sell.doubleValue(), open, close, high, low);
        tradeHistoryRecordRepository.save(tradeHistoryRecord);
    }
}
