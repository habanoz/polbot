package com.habanoz.polbot.core.service;

import com.habanoz.polbot.core.model.PoloniexTrade;

import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 22.04.2017.
 */
public interface TradeTrackerService {
    Map<String, List<PoloniexTrade>> returnTrades(boolean updateRecord);
}
