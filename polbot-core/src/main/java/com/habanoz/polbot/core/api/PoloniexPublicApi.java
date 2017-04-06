package com.habanoz.polbot.core.api;


import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexTicker;

import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 02.04.2017.
 */
public interface PoloniexPublicApi {
    Map<String, PoloniexTicker> returnTicker();

    List<PoloniexChart> returnChart(String currencyPair, Long periodInSeconds, Long startTime, Long endTime);
}
