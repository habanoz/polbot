package com.habanoz.polbot.core.api;

import com.habanoz.polbot.core.model.CoinDeskPrice;

/**
 * Created by huseyina on 5/16/2017.
 */
public interface CoinDeskApi {
    CoinDeskPrice getBtcPrice(String realCurrency);

    CoinDeskPrice getBtcUsdPrice();
}
