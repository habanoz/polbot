package com.habanoz.polbot.core.service;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexTicker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by habanoz on 04.04.2017.
 */
@Component
public class RobotLogicImpl implements RobotLogic {
    @Autowired
    private PoloniexTradingApi tradingApi;

    @Autowired
    private PoloniexPublicApi publicApi;

    //@Scheduled(fixedRate = 60000)
    @Override
    public void runLogic() {
        System.out.println("started");

        Map<String, PoloniexTicker> tickerMap = publicApi.returnTicker();

        TreeMap<String, PoloniexTicker> sortedTickerMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return (int) (tickerMap.get(s1).getBaseVolume().floatValue() - tickerMap.get(s2).getBaseVolume().floatValue());
            }
        });

        sortedTickerMap.putAll(tickerMap);

        for (String key : sortedTickerMap.descendingKeySet()) {
            System.out.println(key+"->"+sortedTickerMap.get(key).getBaseVolume().toString());
        }

        List<PoloniexChart> chartMap = publicApi.returnChart((String) tickerMap.keySet().toArray()[0], 300l, new Date().getTime() / 1000, 9999999999L);


    }
}
