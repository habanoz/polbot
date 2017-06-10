package com.habanoz.polbot.core.api;

import com.habanoz.polbot.core.model.PoloniexTrade;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by huseyina on 6/11/2017.
 */
public class PoloniexPublicApiImplTest {

    public static final String BTC_ETC="BTC_ETC";

    @Test
    public void returnTradeHistory() throws Exception {
        PoloniexPublicApiImpl poloniexPublicApi=new PoloniexPublicApiImpl();
        long end = System.currentTimeMillis() / 1000;
        long start = end - 60 * 10;

        List<PoloniexTrade> tradeHistory = poloniexPublicApi.returnTradeHistory(BTC_ETC, start, end);

    }

}