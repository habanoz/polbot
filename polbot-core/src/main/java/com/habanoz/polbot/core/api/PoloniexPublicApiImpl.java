package com.habanoz.polbot.core.api;

import com.cf.client.HTTPClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.habanoz.polbot.core.model.PoloniexChart;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.model.PoloniexTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by habanoz on 04.04.2017.
 */
@Component
public class PoloniexPublicApiImpl implements PoloniexPublicApi {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexPublicApiImpl.class);

    private static final String PUBLIC_URL = "https://poloniex.com/public?";
    private final HTTPClient client;
    private ObjectMapper objectMapper;
    public static final String DATE_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    public PoloniexPublicApiImpl() {
        client = new HTTPClient();

        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT_STR));

        module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);


        objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(module)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        DateFormat df = new SimpleDateFormat(DATE_FORMAT_STR);
        objectMapper.setDateFormat(df);
    }

    @Override
    public Map<String, PoloniexTicker> returnTicker() {

        try {
            String url = PUBLIC_URL + "command=returnTicker";
            String tickerJsonStr = client.getHttp(url, null);

            return objectMapper.readValue(tickerJsonStr, new TypeReference<HashMap<String, PoloniexTicker>>() {
            });

        } catch (IOException ex) {
            logger.warn("Call to return ticker API resulted in exception - " + ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    public List<PoloniexTrade> returnTradeHistory(String currencyPair, long start, long end) {

        try {
            //https://poloniex.com/public?command=returnTradeHistory&currencyPair=BTC_NXT&start=1410158341&end=1410499372
            String url = PUBLIC_URL + "command=returnTradeHistory&currencyPair=" + currencyPair + "&start=" + start + "&end=" + end;
            String tickerJsonStr = client.getHttp(url, null);

            return objectMapper.readValue(tickerJsonStr, new TypeReference<List<PoloniexTrade>>() {
            });

        } catch (IOException ex) {
            logger.warn("Call to return history API resulted in exception - " + ex.getMessage(), ex);
        }

        return null;
    }


    @Override
    public List<PoloniexChart> returnChart(String currencyPair, Long periodInSeconds, Long startTime, Long endTime) {
        try {
            String url = PUBLIC_URL + "command=returnChartData&currencyPair=" + currencyPair + "&start=" + startTime.toString() + "&end=" + endTime.toString() + "&period=" + periodInSeconds.toString();

            return objectMapper.readValue(client.getHttp(url, null), new TypeReference<List<PoloniexChart>>() {
            });
        } catch (IOException ex) {
            logger.warn("Call to return chart data API resulted in exception - " + ex.getMessage(), ex);
        }

        return null;
    }
}
