package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.entity.CurrencyOrder;
import com.habanoz.polbot.core.entity.UserBot;
import com.habanoz.polbot.core.mail.HtmlHelper;
import com.habanoz.polbot.core.mail.MailService;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.repository.CurrencyOrderRepository;
import com.habanoz.polbot.core.repository.TradeHistoryTrackRepository;
import com.habanoz.polbot.core.repository.UserBotRepository;
import com.habanoz.polbot.core.service.TradeTrackerServiceImpl;
import com.habanoz.polbot.core.utils.ExchangePrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Trading bot with Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
@Component
public class PoloniexPatiencStrategyeBot implements PolBot {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);

    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private CurrencyOrderRepository currencyOrderRepository;

    @Autowired
    private HtmlHelper htmlHelper;

    @Autowired
    private MailService mailService;

    @Autowired
    private TradeHistoryTrackRepository tradeHistoryTrackRepository;

    @Autowired
    private UserBotRepository userBotRepository;

    private static final double minAmount = 0.0001;
    private static final long BUY_SELL_SLEEP = 300;
    private static final String BASE_CURR = "BTC";
    private static final String CURR_PAIR_SEPARATOR = "_";


    public PoloniexPatiencStrategyeBot() {
    }

    @PostConstruct
    public void init() {
    }

    @Scheduled(fixedDelay = 300000)
    @Override
    public void execute() {
        Map<String, PoloniexTicker> tickerMap = publicApi.returnTicker();

        List<BotUser> activeBotUsers = userBotRepository.findByBotQuery(getClass().getSimpleName()).stream().map(UserBot::getUser).collect(Collectors.toList());
        for (BotUser user : activeBotUsers) {
            startTradingForEachUser(user, tickerMap);
        }
    }

    private void startTradingForEachUser(BotUser user, Map<String, PoloniexTicker> tickerMap) {
        logger.info("Started for user {}", user);

        //User specific currency config list
        List<CurrencyConfig> currencyConfigs = currencyConfigRepository.findByBotUser(user)
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
        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();
        Map<String, PoloniexCompleteBalance> completeBalanceMap = tradingApi.returnCompleteBalances();


        Map<String, List<PoloniexTrade>> historyMap = tradingApi.returnTradeHistory();

        Map<String, List<PoloniexTrade>> recentHistoryMap = new TradeTrackerServiceImpl(tradeHistoryTrackRepository, tradingApi, user).returnTrades(true);

        BigDecimal btcBalance = balanceMap.get(BASE_CURR);

        List<PoloniexOrderResult> orderResults = new ArrayList<>();
        HashMap<String, BigDecimal> tradingBTCMap = getBTCTradingMap(currencyConfigs, btcBalance, openOrderMap);


        for (CurrencyConfig currencyConfig : currencyConfigs) {

            String currPair = currencyConfig.getCurrencyPair();
            PatienceStrategy patienceStrategy = new PatienceStrategy(openOrderMap.get(currPair), historyMap.get(currPair));

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

            List<PoloniexOpenOrder> orders = patienceStrategy.execute(
                    currencyConfig,
                    new ExchangePrice(lowestBuyPrice, highestSellPrice, now, ticker.getBaseVolume()),
                    tradingBTCMap.get(currName), currBalance, now
            );

            //fulfill orders
            for (PoloniexOpenOrder order : orders) {
                if (order.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {

                    PoloniexOrderResult result = createBuyOrder(user, tradingApi, order);
                    orderResults.add(result);

                    if (result.getSuccess())
                        // subtract spent(order given) amount from btc balance
                        btcBalance = btcBalance.subtract(order.getTotal());
                } else {
                    PoloniexOrderResult result = createSellOrder(user, tradingApi, order);
                    orderResults.add(result);
                }
            }

            List<PoloniexOpenOrder> orders2Cancel = patienceStrategy.getOrdersToCancel(currencyConfig, now);

            for (PoloniexOpenOrder order2Cancel : orders2Cancel) {
                tradingApi.cancelOrder(order2Cancel.getOrderNumber());
            }

        }

        if (!orderResults.isEmpty() || !recentHistoryMap.isEmpty()) {// if any of them is not empty send mail
            mailService.sendMail(user, "Orders Given", htmlHelper.getSummaryHTML(orderResults, recentHistoryMap, completeBalanceMap), true);
        }


        logger.info("Completed for user {}", user);
    }


    private PoloniexOrderResult createSellOrder(BotUser user, PoloniexTradingApi tradingApi, PoloniexOpenOrder order) {

        PoloniexOrderResult result = tradingApi.sell(order);

        SaveCurrencyTransaction(user, order.getTotal(), order, result);

        return result;
    }

    private PoloniexOrderResult createBuyOrder(BotUser user, PoloniexTradingApi tradingApi, PoloniexOpenOrder order) {

        PoloniexOrderResult result = tradingApi.buy(order);

        SaveCurrencyTransaction(user, order.getTotal(), order, result);

        return result;
    }

    private void SaveCurrencyTransaction(BotUser user, BigDecimal budget, PoloniexOpenOrder openOrder, PoloniexOrderResult result) {
        try {
            if (result.getSuccess()) {

                //TODO: Persistence operation for BUY order so that we can trace and cancel them based on user cancellation day.

                CurrencyOrder currenyOrder = new CurrencyOrder();
                currenyOrder.setUserId(user.getId());
                currenyOrder.setOrderType(openOrder.getType());
                currenyOrder.setCurrencyPair(openOrder.getCurrencyPair());
                currenyOrder.setOrderNumber(result.getTradeResult().getOrderNumber());
                currenyOrder.setOrderDate(Date.class.newInstance());
                currenyOrder.setActive(true);
                currenyOrder.setPrice(openOrder.getRate().floatValue());
                currenyOrder.setAmount(openOrder.getAmount().floatValue());
                currenyOrder.setTotalBtc(budget.floatValue());
                currencyOrderRepository.save(currenyOrder);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        btcBalance = CalculationForUsableBTC(tradingBTCMap, currencyConfigs, btcBalance, openOrderMap, true);
        while (btcBalance.doubleValue() > minAmount) {  // Loop through until the available BTC is over
            double initialBtcValue = btcBalance.doubleValue();
            btcBalance = CalculationForUsableBTC(tradingBTCMap, currencyConfigs, btcBalance, openOrderMap, false);
            if (initialBtcValue == btcBalance.doubleValue()) {
                break;
            }
        }

        if (currencyConfigs.size() > 0 && tradingBTCMap.keySet().size() > 0) {
            Map.Entry<String, BigDecimal> mapKey = tradingBTCMap.entrySet().iterator().next();
            BigDecimal buyBudget = new BigDecimal(btcBalance.doubleValue() + tradingBTCMap.get(mapKey.getKey()).doubleValue());
            tradingBTCMap.put(mapKey.getKey(), buyBudget);
            btcBalance = btcBalance.subtract(buyBudget);
        }

        return tradingBTCMap;
    }


    private BigDecimal CalculationForUsableBTC(HashMap<String, BigDecimal> tradingBTCMap,
                                               List<CurrencyConfig> currencyConfigs,
                                               BigDecimal btcBalance, Map<String,
            List<PoloniexOpenOrder>> openOrderMap,
                                               boolean isMultiplierForEachCurrencyEnabled) {

        if (btcBalance.doubleValue() <= 0) {
            return btcBalance;
        }
        for (CurrencyConfig currencyConfig : currencyConfigs) {


            String currPair = currencyConfig.getCurrencyPair();
            String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];
            List<PoloniexOpenOrder> openOrderListForCurr = openOrderMap.get(currPair);
            // Just calculate BTC value for the currencies who does not have any buy order
            if (openOrderListForCurr != null && openOrderListForCurr.stream().noneMatch(r -> r.getType().equalsIgnoreCase("BUY")) && currencyConfig.getBuyable()) {
                //
                BigDecimal buyBudget = new BigDecimal(minAmount);
                if (isMultiplierForEachCurrencyEnabled) {
                    buyBudget = new BigDecimal(minAmount * currencyConfig.getUsableBalancePercent());
                }
                if (tradingBTCMap.containsKey(currName)) {
                    buyBudget = new BigDecimal(buyBudget.doubleValue() + tradingBTCMap.get(currName).doubleValue());
                    tradingBTCMap.put(currName, buyBudget);
                } else {
                    tradingBTCMap.put(currName, buyBudget);
                }
                if (isMultiplierForEachCurrencyEnabled) {
                    btcBalance = btcBalance.subtract(buyBudget);
                } else {
                    btcBalance = btcBalance.subtract(new BigDecimal(minAmount));
                }
            }
            if (btcBalance.doubleValue() <= minAmount) {
                return btcBalance;
            }
        }
        return btcBalance;
    }
}
