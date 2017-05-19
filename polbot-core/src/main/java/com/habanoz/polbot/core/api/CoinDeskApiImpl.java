package com.habanoz.polbot.core.api;

import com.cf.client.HTTPClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habanoz.polbot.core.model.CoinDeskCurrentPrice;
import com.habanoz.polbot.core.model.CoinDeskPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by huseyina on 5/16/2017.
 */
@Component
public class CoinDeskApiImpl implements CoinDeskApi {
    private static final Logger logger = LoggerFactory.getLogger(CoinDeskApiImpl.class);

    private static final String REAL_CURRENCY_STR = "%REAL_CURRENCY%";
    private static final String PUBLIC_URL = "http://api.coindesk.com/v1/bpi/currentprice/" + REAL_CURRENCY_STR + ".json";
    private final HTTPClient client;

    public CoinDeskApiImpl() {
        client = new HTTPClient();
    }

    @Override
    public CoinDeskPrice getBtcPrice(String realCurrency) {
        try {
            realCurrency = realCurrency.toUpperCase();
            String url = PUBLIC_URL.replace(REAL_CURRENCY_STR, realCurrency);
            String tickerJsonStr = client.getHttp(url, null);
            CoinDeskCurrentPrice coinDeskCurrentPrice = new ObjectMapper().readValue(tickerJsonStr, new TypeReference<CoinDeskCurrentPrice>() {
            });

            return coinDeskCurrentPrice.getBpi().get(realCurrency);

        } catch (IOException ex) {
            logger.warn("Call to coindesk API resulted in exception - " + ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    public CoinDeskPrice getBtcUsdPrice() {
        return getBtcPrice("usd");
    }
}
