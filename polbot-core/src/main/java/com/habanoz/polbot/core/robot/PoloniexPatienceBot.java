package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.model.PoloniexTradeResult;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Created by habanoz on 05.04.2017.
 */
@Component
public class PoloniexPatienceBot {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);
    @Autowired
    private PoloniexTradingApi tradingApi;

    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private BotUserRepository botUserRepository;

    private Iterable<CurrencyConfig> emnCurrencyConfigs;

    private static final double minAmount = 0.0001;
    private static final String BASE_CURR = "BTC";
    private static final String CURR_PAIR_SEPARATOR = "_";


    public PoloniexPatienceBot() {

    }

    @PostConstruct
    public void init() {
        emnCurrencyConfigs = currencyConfigRepository.findAll();
    }

    @Scheduled(fixedDelay = 60000)
    public void runLogic() {
        logger.info("Started");

        Map<String, PoloniexTicker> tickerMap = publicApi.returnTicker();

        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();

        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();

        Map<String, List<PoloniexTrade>> historyMap = tradingApi.returnTradeHistory();

        BigDecimal btcBalance = balanceMap.get(BASE_CURR);

        for (CurrencyConfig currencyConfig : emnCurrencyConfigs) {

            String currPair = currencyConfig.getCurrencyPair();
            String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];

            BigDecimal currBalance = balanceMap.get(currName);

            List<PoloniexOpenOrder> openOrderListForCurr = openOrderMap.get(currPair);

            PoloniexTicker ticker = tickerMap.get(currPair);

            // this may indicate invalid currency name
            if (ticker == null)
                continue;

            //current lowest market price
            BigDecimal lowestBuyPrice = ticker.getLowestAsk();
            BigDecimal highestSellPrice = ticker.getHighestBid();

            // only pre-defined percentage of available balance can be used for buying a currency
            BigDecimal buyBudget = new BigDecimal(btcBalance.doubleValue() * currencyConfig.getUsableBalancePercent() * 0.01);

            if (buyBudget.doubleValue() < minAmount && btcBalance.doubleValue() >= minAmount) {
                buyBudget = new BigDecimal(minAmount);
            }

            //
            //
            // buy logic
            if (currencyConfig.getUsableBalancePercent() > 0 && currencyConfig.getBuyable() && openOrderListForCurr.isEmpty() && buyBudget.doubleValue() > minAmount) {

                // buying price should be a little lower to make profit
                // if set, buy at price will be used, other wise buy on percent will be used
                BigDecimal buyPrice = currencyConfig.getBuyAtPrice() == 0 ? new BigDecimal(lowestBuyPrice.doubleValue() * (100 - currencyConfig.getBuyOnPercent()) * 0.01) : new BigDecimal(currencyConfig.getBuyAtPrice());

                // calculate amount that can be bought with buyBudget and buyPrice
                BigDecimal buyAmount = buyBudget.divide(buyPrice, RoundingMode.DOWN);

                PoloniexTradeResult result = tradingApi.buy(currPair, buyPrice, buyAmount);

                if (result != null) {
                    btcBalance = btcBalance.subtract(buyBudget);
                }
            }

            //
            //
            // sell logic
            if (currencyConfig.getSellable() && currBalance.doubleValue() > minAmount) {
                List<PoloniexTrade> currHistoryList = historyMap.get(currPair);

                // get last buying price to calculate selling price
                BigDecimal lastBuyPrice = currHistoryList.get(0).getRate();
                final BigDecimal sellAmount = currBalance;

                for (PoloniexTrade history : currHistoryList) {
                    if (history.getType().equalsIgnoreCase("buy")) {
                        lastBuyPrice = history.getRate();
                    }
                }

                //selling price should be a little higher to make profit
                // if set, sell at price will be used, otherwise sell on percent will be used
                BigDecimal sellPrice = currencyConfig.getSellAtPrice() == 0 ? new BigDecimal(lastBuyPrice.doubleValue() * (100 + currencyConfig.getSellOnPercent()) * 0.01) : new BigDecimal(currencyConfig.getSellAtPrice());

                PoloniexTradeResult result = tradingApi.sell(currPair, sellPrice, sellAmount);
            }
        }

        logger.info("Completed");

    }
}
