package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.entity.CurrenyOrder;
import com.habanoz.polbot.core.entity.UserBot;
import com.habanoz.polbot.core.mail.HtmlHelper;
import com.habanoz.polbot.core.mail.MailService;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.repository.CurrenyOrderRepository;
import com.habanoz.polbot.core.repository.TradeHistoryTrackRepository;
import com.habanoz.polbot.core.repository.UserBotRepository;
import com.habanoz.polbot.core.service.TradeTrackerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trading bot with Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
@Component
public class PoloniexPatienceBot {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);

    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private CurrenyOrderRepository currenyOrderRepository;

    @Autowired
    private HtmlHelper htmlHelper;

    @Autowired
    private MailService mailService;

    @Autowired
    private TradeHistoryTrackRepository tradeHistoryTrackRepository;

    @Autowired
    private UserBotRepository userBotRepository;

    private static final double minAmount = 0.0001;
    private static final long BUY_SELL_SLEEP = 100;
    private static final String BASE_CURR = "BTC";
    private static final String CURR_PAIR_SEPARATOR = "_";


    public PoloniexPatienceBot() {
    }

    @PostConstruct
    public void init() {
    }

    @Scheduled(fixedDelay = 300000)
    public void runLogic() {
        Map<String, PoloniexTicker> tickerMap = publicApi.returnTicker();

        List<BotUser> activeBotUsers = userBotRepository.findEnabledUsersByBotQuery(getClass().getSimpleName()).stream().map(UserBot::getUser).collect(Collectors.toList());
        for (BotUser user : activeBotUsers) {
            startTradingForEachUser(user, tickerMap);
        }
    }

    private void startTradingForEachUser(BotUser user, Map<String, PoloniexTicker> tickerMap) {
        logger.info("Started for user {}", user);

        //User specific currency config list
        List<CurrencyConfig> currencyConfigs = currencyConfigRepository.findByUserId(user.getUserId())
                .stream().filter(r -> r.getBuyable() || r.getSellable())
                .sorted((f1, f2) -> Float.compare(f1.getUsableBalancePercent(), f2.getUsableBalancePercent()))
                .collect(Collectors.toList());

        if (currencyConfigs.isEmpty()) {
            logger.info("No currency config for user {}, returning ...", user);
            return;
        }

        //create tradingApi instance for current user
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(user);

        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();

        // TODO: Cancel BUY orders based on user's buy cancellation day value. NO need to wait a unfilled buy orders
        for (Map.Entry<String, List<PoloniexOpenOrder>> mapKey : openOrderMap.entrySet()) {
            String key = mapKey.getKey();
            List<PoloniexOpenOrder> ordersForEachCurrency = mapKey.getValue();
            for (PoloniexOpenOrder order : ordersForEachCurrency) {
                if (order.getType().equalsIgnoreCase("BUY")) {

                }
            }
        }

        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();
        Map<String, PoloniexCompleteBalance> completeBalanceMap = tradingApi.returnCompleteBalances();


        Map<String, List<PoloniexTrade>> historyMap = tradingApi.returnTradeHistory();

        Map<String, List<PoloniexTrade>> recentHistoryMap = new TradeTrackerServiceImpl(tradeHistoryTrackRepository, tradingApi, user).returnTrades(true);

        BigDecimal btcBalance = balanceMap.get(BASE_CURR);
        Double allBtcProperty = completeBalanceMap.values().stream().mapToDouble(PoloniexCompleteBalance::getBtcValue).sum();

        List<PoloniexOrderResult> orderResults = new ArrayList<>();
        HashMap<String, BigDecimal> tradingBTCMap  = getBTCTradingMap(currencyConfigs, btcBalance, openOrderMap);
        for (CurrencyConfig currencyConfig : currencyConfigs) {

            String currPair = currencyConfig.getCurrencyPair();
            String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];

            BigDecimal currBalance = balanceMap.get(currName);

            List<PoloniexOpenOrder> openOrderListForCurr = openOrderMap.get(currPair);

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


            //
            //
            // buy logic
            if (currencyConfig.getUsableBalancePercent() > 0 &&
                    currencyConfig.getBuyable() &&
                    openOrderListForCurr.stream().noneMatch(r -> r.getType().equalsIgnoreCase("BUY"))) {

                BigDecimal spent = createBuyOrder(user, tradingApi, btcBalance, allBtcProperty, orderResults, currencyConfig, currPair, lowestBuyPrice,tradingBTCMap);

                //update balance
                btcBalance = btcBalance.subtract(spent);

                sleep();
            }

            //
            //
            // sell logic
            if (currencyConfig.getSellable() && currBalance.doubleValue() > minAmount) {
                List<PoloniexTrade> currHistoryList = historyMap.get(currPair);

                createSellOrder(tradingApi, orderResults, currencyConfig, currPair, currBalance, highestSellPrice, currHistoryList);

                sleep();
            }

        }

        if (!orderResults.isEmpty() || !recentHistoryMap.isEmpty())// if any of them is not empty send mail
            mailService.sendMail(user.getUserEmail(), "Orders Given", htmlHelper.getSummaryHTML(orderResults, recentHistoryMap, tradingApi.returnCompleteBalances()), true);


        logger.info("Completed for user {}", user);
    }

    private void createSellOrder(PoloniexTradingApi tradingApi, List<PoloniexOrderResult> orderResults, CurrencyConfig currencyConfig, String currPair, BigDecimal currBalance, BigDecimal highestSellPrice, List<PoloniexTrade> currHistoryList) {
        // get last buying price to calculate selling price

        BigDecimal lastBuyPrice = highestSellPrice;
        final BigDecimal sellAmount = currBalance;

        if (currHistoryList.get(0) != null) {

            for (PoloniexTrade history : currHistoryList) {
                // if remaining history records are too old, dont use them for selling price base
                if (history.getDate().plus(1, ChronoUnit.WEEKS).isBefore(LocalDateTime.now()))
                    break;

                // use most recent buy action as sell base
                if (history.getType().equalsIgnoreCase("buy")) {
                    lastBuyPrice = history.getRate();
                    break;
                }
            }
        }

        //selling price should be a little higher to make profit
        // if set, sell at price will be used, otherwise sell on percent will be used
        BigDecimal sellPrice = currencyConfig.getSellAtPrice() == 0 ? new BigDecimal(lastBuyPrice.doubleValue() * (100 + currencyConfig.getSellOnPercent()) * 0.01) : new BigDecimal(currencyConfig.getSellAtPrice());

        PoloniexOpenOrder openOrder = new PoloniexOpenOrder(currPair, "SELL", sellPrice, sellAmount);
        PoloniexOrderResult result = tradingApi.sell(openOrder);

        orderResults.add(result);
    }

    private BigDecimal createBuyOrder(BotUser user,
                                      PoloniexTradingApi tradingApi,
                                      BigDecimal btcBalance,
                                      Double allBtcProperty,
                                      List<PoloniexOrderResult> orderResults,
                                      CurrencyConfig currencyConfig,
                                      String currPair,
                                      BigDecimal lowestBuyPrice,        HashMap<String, BigDecimal> tradingBTCMap) {
        String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];
        BigDecimal buyBudget =  tradingBTCMap.get(currName);
        // not enough budget, return 0
        if (buyBudget.doubleValue() < minAmount) {
            return BigDecimal.valueOf(0);
        }

        // buying price should be a little lower to make profit
        // if set, buy at price will be used, other wise buy on percent will be used
        BigDecimal buyPrice = currencyConfig.getBuyAtPrice() == 0 ? new BigDecimal(lowestBuyPrice.doubleValue() * (100 - currencyConfig.getBuyOnPercent()) * 0.01) : new BigDecimal(currencyConfig.getBuyAtPrice());

        // calculate amount that can be bought with buyBudget and buyPrice
        BigDecimal buyAmount = buyBudget.divide(buyPrice, RoundingMode.DOWN);

        PoloniexOpenOrder openOrder = new PoloniexOpenOrder(currPair, "BUY", buyPrice, buyAmount);
        PoloniexOrderResult result = tradingApi.buy(openOrder);



        orderResults.add(result);

        return new BigDecimal(buyBudget.doubleValue());
    }

    private void sleep() {
        try {
            Thread.sleep(BUY_SELL_SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, BigDecimal> getBTCTradingMap(List<CurrencyConfig> currencyConfigs, BigDecimal btcBalance, Map<String, List<PoloniexOpenOrder>> openOrderMap) {
        HashMap<String, BigDecimal> tradingBTCMap = new HashMap<>();

        btcBalance = CalculationForUsableBTC(tradingBTCMap, currencyConfigs, btcBalance, openOrderMap, true );
        while (btcBalance.doubleValue() > minAmount) {  // Loop through until the available BTC is over
            double initialBtcValue = btcBalance.doubleValue();
            btcBalance = CalculationForUsableBTC(tradingBTCMap, currencyConfigs, btcBalance, openOrderMap, false);
            if(initialBtcValue == btcBalance.doubleValue()){
                break;
            }
        }

        if(currencyConfigs.size()>0 && tradingBTCMap.keySet().size() > 0){
            Map.Entry<String, BigDecimal> mapKey = tradingBTCMap.entrySet().iterator().next();
            BigDecimal  buyBudget = new BigDecimal( btcBalance.doubleValue() + tradingBTCMap.get(mapKey.getKey()).doubleValue());
            tradingBTCMap.put(mapKey.getKey(),buyBudget);
            btcBalance = btcBalance.subtract(buyBudget);
        }
        return tradingBTCMap;
    }


    private BigDecimal CalculationForUsableBTC(HashMap<String, BigDecimal> tradingBTCMap,
                                               List<CurrencyConfig> currencyConfigs,
                                               BigDecimal btcBalance, Map<String,
            List<PoloniexOpenOrder>> openOrderMap,
                                               boolean isMultiplierForEachCurrencyEnabled) {

        if(btcBalance.doubleValue() <= 0){
            return btcBalance;
        }
        for (CurrencyConfig currencyConfig : currencyConfigs) {


            String currPair = currencyConfig.getCurrencyPair();
            String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];
            List<PoloniexOpenOrder> openOrderListForCurr = openOrderMap.get(currPair);
            // Just calculate BTC value for the currencies who does not have any buy order
            if (!openOrderListForCurr.stream().anyMatch(r -> r.getType().equalsIgnoreCase("BUY"))) {
                //
                BigDecimal buyBudget = new BigDecimal(minAmount);
                if(isMultiplierForEachCurrencyEnabled){
                    buyBudget = new BigDecimal(minAmount * currencyConfig.getUsableBalancePercent());
                }
                if (tradingBTCMap.containsKey(currName)) {
                    buyBudget = new BigDecimal( buyBudget.doubleValue() + tradingBTCMap.get(currName).doubleValue());
                    tradingBTCMap.put(currName, buyBudget);
                } else {
                    tradingBTCMap.put(currName, buyBudget);
                }
                if(isMultiplierForEachCurrencyEnabled){
                    btcBalance = btcBalance.subtract(buyBudget);
                }else{
                    btcBalance = btcBalance.subtract(new BigDecimal(minAmount));
                }
            }
            if(btcBalance.doubleValue() <= minAmount){
                return btcBalance;
            }
        }
        return btcBalance;
    }
}
