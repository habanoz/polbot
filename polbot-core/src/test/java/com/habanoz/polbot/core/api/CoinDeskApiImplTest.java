package com.habanoz.polbot.core.api;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huseyina on 5/19/2017.
 */
public class CoinDeskApiImplTest {
    @Test
    public void getBtcUsdPrice() throws Exception {
        CoinDeskApi coinDeskApi=new CoinDeskApiImpl();
        coinDeskApi.getBtcUsdPrice();
    }

}