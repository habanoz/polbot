package com.habanoz.polbot.core.api;

import com.cf.client.HTTPClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 04.04.2017.
 */
@Component
public class PoloniexPublicApiImpl implements PoloniexPublicApi {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexPublicApiImpl.class);

    private static final String PUBLIC_URL = "https://poloniex.com/public?";
    private final HTTPClient client;

    public PoloniexPublicApiImpl() {
        client = new HTTPClient();
    }

    @Override
    public Map<String, PoloniexTicker> returnTicker() {

        try {
            String url = PUBLIC_URL + "command=returnTicker";
            String tickerJsonStr = client.getHttp(url, null);
            return new ObjectMapper().readValue(tickerJsonStr, new TypeReference<HashMap<String, PoloniexTicker>>() {
            });
        } catch (IOException ex) {
            logger.warn("Call to return ticker API resulted in exception - " + ex.getMessage(), ex);
        }

        return null;
    }


    @Override
    public List<PoloniexChart> returnChart(String currencyPair, Long periodInSeconds, Long startTime, Long endTime) {
        try {
            String url = PUBLIC_URL + "command=returnChartData&currencyPair=" + currencyPair + "&start=" + startTime.toString() + "&end=" + endTime.toString() + "&period=" + periodInSeconds.toString();

            return new ObjectMapper().readValue(client.getHttp(url, null), new TypeReference<List<PoloniexChart>>() {
            });
        } catch (IOException ex) {
            logger.warn("Call to return chart data API resulted in exception - " + ex.getMessage(), ex);
        }

        return null;
    }
}
