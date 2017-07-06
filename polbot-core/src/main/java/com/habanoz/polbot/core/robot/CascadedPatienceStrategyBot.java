package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.mail.HtmlHelper;
import com.habanoz.polbot.core.mail.MailService;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.repository.TradeHistoryTrackRepository;
import com.habanoz.polbot.core.service.TradeTrackerServiceImpl;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trading bot with Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
@Component
public class CascadedPatienceStrategyBot extends PoloniexPatienceStrategyBot {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private HtmlHelper htmlHelper;

    @Autowired
    private MailService mailService;

    @Autowired
    private TradeHistoryTrackRepository tradeHistoryTrackRepository;

    private static final String BASE_CURR = "BTC";
    private static final String CURR_PAIR_SEPARATOR = "_";


    public CascadedPatienceStrategyBot() {
    }

    @PostConstruct
    public void init() {
    }

    @Scheduled(fixedDelay = 300000)
    @Override
    public void execute() {
        super.execute();
    }

    @Override
    public void startTradingForEachUser(BotUser user, Map<String, PoloniexTicker> tickerMap) {
        logger.info("Started for user {}", user);

        List<CurrencyConfig> currencyConfigs = getCurrencyConfigs(user);

        if (currencyConfigs.isEmpty()) {
            logger.info("No currency config for user {}, returning ...", user);
            return;
        }

        //create tradingApi instance for current user
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(user);


        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();
        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();
        Map<String, PoloniexCompleteBalance> completeBalanceMap = tradingApi.returnCompleteBalances();


        Map<String, List<PoloniexTrade>> historyMap = tradingApi.returnTradeHistory();

        Map<String, List<PoloniexTrade>> recentHistoryMap = new TradeTrackerServiceImpl(tradeHistoryTrackRepository, tradingApi, user).returnTrades(true);

        BigDecimal btcBalance = balanceMap.get(BASE_CURR);

        List<PoloniexOrderResult> orderResults = new ArrayList<>();

        for (CurrencyConfig currencyConfig : currencyConfigs) {

            String currPair = currencyConfig.getCurrencyPair();

            PolStrategy patienceStrategy = getPolStrategy(openOrderMap.get(currPair), historyMap.get(currPair), recentHistoryMap.get(currPair));

            String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];

            BigDecimal currBalance = balanceMap.get(currName);

            // this may indicate invalid currency name
            if (tickerMap == null)
                continue;

            PoloniexTicker ticker = tickerMap.get(currPair);

            // this may indicate invalid currency name
            if (ticker == null)
                continue;

            //current lowest market price
            BigDecimal lowestBuyPrice = ticker.getLowestAsk();
            BigDecimal highestSellPrice = ticker.getHighestBid();

            Date now = new Date();

            List<Order> orders = patienceStrategy.execute(
                    currencyConfig,
                    new ExchangePrice(lowestBuyPrice, highestSellPrice, now, ticker.getBaseVolume()),
                    btcBalance, currBalance, now
            );

            btcBalance = createOrders(user, tradingApi, btcBalance, orderResults, orders);

            cancelOrders(tradingApi, currencyConfig, patienceStrategy, now);

        }

        sendNotificationMail(user, completeBalanceMap, recentHistoryMap, orderResults);

        logger.info("Completed for user {}", user);
    }


    @Override
    public PolStrategy getPolStrategy(List<PoloniexOpenOrder> openOrderList, List<PoloniexTrade> tradeList, List<PoloniexTrade> historyList) {
        return new CascadedPatienceStrategy(openOrderList, tradeList, historyList);
    }
}
