package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.config.EmnCurrencyConfig;
import com.habanoz.polbot.core.config.EmnCurrencyConfigParser;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.model.PoloniexTradeHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 05.04.2017.
 */
@Component
public class PoloniexPatienceBot {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTradeHistory.class);
    @Autowired
    private PoloniexTradingApi tradingApi;

    @Autowired
    private PoloniexPublicApi publicApi;

    private List<EmnCurrencyConfig> emnCurrencyConfigs;
    private double minBuyBtcBudget = 0.0001;

    private String BASE_CURR = "BTC";
    private final String CURR_PAIR_SEPARATOR = "_";


    public PoloniexPatienceBot() {
        emnCurrencyConfigs = new EmnCurrencyConfigParser().parse(new File("currency.config"));
    }

    @Scheduled(fixedDelay = 60000)
    public void runLogic() {
        logger.info("Started");

        Map<String, PoloniexTicker> tickerMap = publicApi.returnTicker();

        Map<String, Float> balanceMap = tradingApi.returnBalances();

        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();

        Map<String, List<PoloniexTradeHistory>> historyMap = tradingApi.returnTradeHistory();

        float btcBalance = balanceMap.get(BASE_CURR);

        for (EmnCurrencyConfig currencyConfig : emnCurrencyConfigs) {
            String currName = currencyConfig.getCurrencyName();
            String currPair = BASE_CURR + CURR_PAIR_SEPARATOR + currName;

            if (currencyConfig.getUsableBalancePercent() <= 0)
                continue;

            float currBalance = balanceMap.get(currName);

            List<PoloniexOpenOrder> openOrderListForCurr = openOrderMap.get(currPair);

            PoloniexTicker ticker = tickerMap.get(currPair);

            //current lowest market price
            BigDecimal lowestBuyPrice = ticker.getLowestAsk();
            BigDecimal highestSellPrice = ticker.getHighestBid();

            // only pre-defined percentage of available balance can be used for buying a currency
            BigDecimal buyBudget = new BigDecimal(btcBalance * currencyConfig.getUsableBalancePercent() * 0.01);

            if (buyBudget.doubleValue() < minBuyBtcBudget && btcBalance >= minBuyBtcBudget) {
                buyBudget = new BigDecimal(minBuyBtcBudget);
            }

            // buying price should be a little lower to make profit
            BigDecimal buyPrice = new BigDecimal(lowestBuyPrice.doubleValue() * (100 - currencyConfig.getBuyOnPercent()) * 0.01);


            // calculate amount that can be bouht with buyBudget and buyPrice
            BigDecimal buyAmount = buyBudget.divide(buyPrice, RoundingMode.DOWN);

            // buy logic
            if (openOrderListForCurr.isEmpty() && buyBudget.doubleValue() > minBuyBtcBudget) {
                String result = tradingApi.buy(currPair, buyPrice, buyAmount);

                logger.info(result);

                btcBalance -= buyBudget.floatValue();

                logger.info("Buy order for {} at rate {} of amount {}", currPair, buyPrice.floatValue(), buyAmount.floatValue());
            }

            // sell logic
            if (currBalance > 0 && openOrderListForCurr.stream().noneMatch(p -> p.getType().equalsIgnoreCase("sell"))) {
                List<PoloniexTradeHistory> currHistoryList = historyMap.get(currPair);

                // get last buying price to calculate selling price
                BigDecimal lastBuyPrice = currHistoryList.get(0).getRate();
                BigDecimal sellAmount = new BigDecimal(currBalance);
                for (PoloniexTradeHistory history : currHistoryList) {
                    if (history.getType().equalsIgnoreCase("buy")) {
                        lastBuyPrice = history.getRate();
                    }
                }

                //selling price should be a little higher to make profit
                BigDecimal sellPrice = new BigDecimal(lastBuyPrice.doubleValue() * (100 + currencyConfig.getSellOnPercent() + currencyConfig.getBuyOnPercent()) * 0.01);


                String result = tradingApi.sell(currPair, sellPrice, sellAmount);

                logger.info(result);

                logger.info("Sell order for {} at rate {} of amount {}", currPair, sellPrice.floatValue(), sellAmount.floatValue());
            }

        }

        logger.info("Completed");

    }
}
