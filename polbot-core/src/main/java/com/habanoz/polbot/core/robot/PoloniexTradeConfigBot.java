package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexPublicApiImpl;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.*;
import com.habanoz.polbot.core.mail.HtmlHelper;
import com.habanoz.polbot.core.mail.MailService;
import com.habanoz.polbot.core.model.*;
import com.habanoz.polbot.core.repository.*;
import com.habanoz.polbot.core.service.TradeTrackerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Trading bot with Buy when cheap sell when high logic
 * <p>
 * Created by habanoz on 05.04.2017.
 */
@Component
public class PoloniexTradeConfigBot implements PolBot {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTrade.class);
    public static final double MIN_COIN_BUY_AMOUNT = 0.05;

    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private BotTradeConfigRepository botTradeConfigRepository;

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

    private static final String CURR_PAIR_SEPARATOR = "_";


    public PoloniexTradeConfigBot() {
    }

    @PostConstruct
    public void init() {
    }

    @Scheduled(fixedDelay = 300000)
    @Override
    @Transactional
    public void execute() {
        Map<String, PoloniexTicker> tickerMap = publicApi.returnTicker();

        List<BotUser> activeBotUsers = userBotRepository.findByBotQuery(getClass().getSimpleName()).stream().map(UserBot::getUser).collect(Collectors.toList());
        for (BotUser user : activeBotUsers) {
            startTradingForEachUser(user, tickerMap);
        }
    }

    @Override
    public void startTradingForEachUser(BotUser user, Map<String, PoloniexTicker> tickerMap) {
        logger.info("Started for user {}", user);

        List<BotTradeConfig> botTradeConfigs = botTradeConfigRepository.findByBotUserAndCompleted(user, 0);


        if (botTradeConfigs.isEmpty()) {
            logger.info("No currency config for user {}, returning ...", user);
            return;
        }

        //create tradingApi instance for current user
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(user);

        PoloniexPublicApi publicApi = new PoloniexPublicApiImpl();

        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();
        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();
        Map<String, PoloniexCompleteBalance> completeBalanceMap = tradingApi.returnCompleteBalances();


        Map<String, List<PoloniexTrade>> historyMap = tradingApi.returnTradeHistory();

        Map<String, List<PoloniexTrade>> recentHistoryMap = new TradeTrackerServiceImpl(tradeHistoryTrackRepository, tradingApi, user).returnTrades(true);


        List<PoloniexOrderResult> orderResults = new ArrayList<>();


        List<Order> orders = new ArrayList<>();

        for (BotTradeConfig botTradeConfig : botTradeConfigs) {

            float currencyBalance = botTradeConfig.getUsableBalance();
            String currPair = botTradeConfig.getCurrencyPair();
            String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];
            BigDecimal coinBalance = balanceMap.get(currName);

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

            // if below stop loss sell
            if (highestSellPrice.compareTo(botTradeConfig.getStopLossPrice()) <= 0) {

                botTradeConfig.setCompleted(1);
                botTradeConfig.setStatus("Stop");
                botTradeConfig.setUpdated(now);

                cancelOpenOrders(tradingApi, openOrderMap, currPair);
                if (coinBalance.compareTo(BigDecimal.valueOf(0))>0){
                    Order order = new Order(currPair, PolBot.SELL_ACTION, lowestBuyPrice.multiply(BigDecimal.valueOf(0.90)), coinBalance);// sell a little lower to ensure it is sold
                    orders.add(order);
                }

                continue;
            }

            // if timeout, cancel all open orders and close the config
            if (cancelOrders(botTradeConfig, now)) {
                botTradeConfig.setCompleted(1);
                botTradeConfig.setStatus("timeout");
                botTradeConfig.setUpdated(now);

                cancelOpenOrders(tradingApi, openOrderMap, currPair);

                continue;
            }

            // all orders completed
            if (botTradeConfig.getSellMode() == 1 && openOrderMap.get(currPair).isEmpty()) {
                botTradeConfig.setCompleted(1);
                botTradeConfig.setStatus("soldout");
                botTradeConfig.setUpdated(now);

                continue;
            }


            // danger of loosing profit, sell every thing
            // after entering sell mode, and declining to buy prices, sell at high buying price
            if (botTradeConfig.getSellMode() == 1 && botTradeConfig.getSellOrderGiven() == 1 && lowestBuyPrice.compareTo(botTradeConfig.getBuyAtPriceHigh()) <= 0) {
                botTradeConfig.setCompleted(1);
                botTradeConfig.setStatus("danger");
                botTradeConfig.setUpdated(now);

                cancelOpenOrders(tradingApi, openOrderMap, currPair);

                Order order = new Order(currPair, PolBot.SELL_ACTION, lowestBuyPrice.multiply(botTradeConfig.getBuyAtPriceHigh()), coinBalance);// sell a little lower to ensure it is sold
                orders.add(order);

                continue;
            }


            // if it is not sell mode and price is less than sell mode price and no buy orders given yet and no open orders
            if (botTradeConfig.getSellMode() == 0 && lowestBuyPrice.compareTo(botTradeConfig.getSellModePrice()) <= 0 && botTradeConfig.getBuyOrderGiven() == 0 && (openOrderMap.get(currPair) == null || openOrderMap.get(currPair).isEmpty())) {
                int numberOfLowSplits = botTradeConfig.getBuySplitHalfCount();

                BigDecimal amountOfBalanceLeft = BigDecimal.valueOf(currencyBalance);
                BigDecimal amountOfBalancePerBuy;
                BigDecimal priceOffsetHigh;
                BigDecimal priceOffsetLow;
                if (numberOfLowSplits == 0) {

                    amountOfBalancePerBuy = amountOfBalanceLeft;
                    priceOffsetHigh = BigDecimal.valueOf(0);
                    priceOffsetLow = BigDecimal.valueOf(0);
                } else {
                    amountOfBalancePerBuy = amountOfBalanceLeft.divide(BigDecimal.valueOf(numberOfLowSplits * 2), new MathContext(5));
                    priceOffsetHigh = botTradeConfig.getBuyAtPriceHigh().subtract(botTradeConfig.getBuyAtPrice()).divide(BigDecimal.valueOf(numberOfLowSplits), new MathContext(5));
                    priceOffsetLow = botTradeConfig.getBuyAtPrice().subtract(botTradeConfig.getBuyAtPriceLow()).divide(BigDecimal.valueOf(numberOfLowSplits), new MathContext(5));
                }


                for (int i = 0; i < numberOfLowSplits; i++) {
                    BigDecimal buyPrice = botTradeConfig.getBuyAtPriceHigh().subtract(priceOffsetHigh.multiply(BigDecimal.valueOf(i)));
                    Order order = new Order(currPair, PolBot.BUY_ACTION, buyPrice, amountOfBalancePerBuy.divide(buyPrice, new MathContext(5)));
                    amountOfBalanceLeft = amountOfBalanceLeft.subtract(amountOfBalancePerBuy);

                    orders.add(order);
                }


                for (int i = 1; i < numberOfLowSplits; i++) {
                    BigDecimal buyPrice = botTradeConfig.getBuyAtPrice().subtract(priceOffsetLow.multiply(BigDecimal.valueOf(i)));
                    Order order = new Order(currPair, PolBot.BUY_ACTION, buyPrice, amountOfBalancePerBuy.divide(buyPrice, new MathContext(5)));
                    amountOfBalanceLeft = amountOfBalanceLeft.subtract(amountOfBalancePerBuy);

                    orders.add(order);
                }

                Order order = new Order(currPair, PolBot.BUY_ACTION, botTradeConfig.getBuyAtPriceLow(), amountOfBalanceLeft.divide(botTradeConfig.getBuyAtPrice(), new MathContext(5)));
                orders.add(order);

                botTradeConfig.setBuyOrderGiven(1);
            }

            // sell mode not set, buy orders already given, sell orders not given yet, current price is above sell mode price and coin balance is above minimal possible amount
            if (botTradeConfig.getSellMode() == 0 && botTradeConfig.getBuyOrderGiven() == 1 && botTradeConfig.getSellOrderGiven() == 0 && lowestBuyPrice.compareTo(botTradeConfig.getSellModePrice()) > 0 && coinBalance.compareTo(BigDecimal.valueOf(0)) > MIN_COIN_BUY_AMOUNT) {//switch to sell mode
                cancelOpenOrders(tradingApi, openOrderMap, currPair);

                int numberOfLowSplits = botTradeConfig.getSellSplitHalfCount();

                // ensure that small amount of coin is not divided into many parts for selling
                numberOfLowSplits = Math.min(coinBalance.divide(BigDecimal.valueOf(MIN_COIN_BUY_AMOUNT), new MathContext(5)).intValue(), numberOfLowSplits);

                BigDecimal amountOfCoinsLeft = coinBalance;

                BigDecimal amountOfCoinsPerSell;
                BigDecimal priceOffsetHigh;
                BigDecimal priceOffsetLow;

                if (numberOfLowSplits == 0) {

                    amountOfCoinsPerSell = amountOfCoinsLeft;
                    priceOffsetHigh = BigDecimal.valueOf(0);
                    priceOffsetLow = BigDecimal.valueOf(0);
                } else {
                    amountOfCoinsPerSell = coinBalance.divide(BigDecimal.valueOf(numberOfLowSplits * 2), new MathContext(5));
                    priceOffsetHigh = botTradeConfig.getSellAtPriceHigh().subtract(botTradeConfig.getSellAtPrice()).divide(BigDecimal.valueOf(numberOfLowSplits), new MathContext(5));
                    priceOffsetLow = botTradeConfig.getSellAtPrice().subtract(botTradeConfig.getSellAtPriceLow()).divide(BigDecimal.valueOf(numberOfLowSplits), new MathContext(5));
                }


                for (int i = 0; i < numberOfLowSplits; i++) {
                    BigDecimal sellPrice = botTradeConfig.getSellAtPriceHigh().subtract(priceOffsetHigh.multiply(BigDecimal.valueOf(i)));
                    Order order = new Order(currPair, PolBot.SELL_ACTION, sellPrice, amountOfCoinsPerSell);
                    amountOfCoinsLeft = amountOfCoinsLeft.subtract(amountOfCoinsPerSell);

                    orders.add(order);
                }


                for (int i = 1; i < numberOfLowSplits; i++) {
                    BigDecimal sellPrice = botTradeConfig.getSellAtPrice().subtract(priceOffsetLow.multiply(BigDecimal.valueOf(i)));
                    Order order = new Order(currPair, PolBot.SELL_ACTION, sellPrice, amountOfCoinsPerSell);
                    amountOfCoinsLeft = amountOfCoinsLeft.subtract(amountOfCoinsPerSell);

                    orders.add(order);
                }

                Order order = new Order(currPair, PolBot.SELL_ACTION, botTradeConfig.getSellAtPriceLow(), amountOfCoinsLeft);
                orders.add(order);

                botTradeConfig.setSellMode(1);
                botTradeConfig.setSellOrderGiven(1);
            }
        }

        createOrders(user, tradingApi, null, orderResults, orders);

        sendNotificationMail(user, completeBalanceMap, recentHistoryMap, orderResults);


        logger.info("Completed for user {}", user);
    }

    private void cancelOpenOrders(PoloniexTradingApi tradingApi, Map<String, List<PoloniexOpenOrder>> openOrderMap, String currPair) {
        //cancel all orders
        List<PoloniexOpenOrder> openOrders = openOrderMap.get(currPair);
        for (PoloniexOpenOrder order2Cancel : openOrders) {
            tradingApi.cancelOrder(order2Cancel.getOrderNumber());

            logger.debug("Order {} cancelled because mode switch to sell", order2Cancel);
        }
    }


    public boolean cancelOrders(BotTradeConfig botTradeConfig, Date now) {
        float orderTimeoutInHour = botTradeConfig.getOrderTimeoutInHour();
        return orderTimeoutInHour > 0 && (now.getTime() - botTradeConfig.getCreated().getTime()) > orderTimeoutInHour * 1000 * 60 * 60;

    }

    @Override
    public List<CurrencyConfig> getCurrencyConfigs(BotUser user) {
        return null;
    }

    @Override
    public void sendNotificationMail(BotUser user, Map<String, PoloniexCompleteBalance> completeBalanceMap, Map<String, List<PoloniexTrade>> recentHistoryMap, List<PoloniexOrderResult> orderResults) {
        if ((!orderResults.isEmpty() || !recentHistoryMap.isEmpty()) && user.isEmailNotification()) {// if any of them is not empty send mail
            mailService.sendMail(user, "Orders Given", htmlHelper.getSummaryHTML(orderResults, recentHistoryMap, completeBalanceMap), true);
        }
    }

    public List<PoloniexOpenOrder> getOrdersToCancel(List<PoloniexOpenOrder> openOrderList, Date date, float orderTimeoutInHour) {

        Iterator<PoloniexOpenOrder> openOrderIterator = openOrderList.iterator();
        List<PoloniexOpenOrder> openOrdersToCancel = new ArrayList<>();
        while (openOrderIterator.hasNext()) {
            PoloniexOpenOrder openOrder = openOrderIterator.next();
            if (orderTimeoutInHour > 0 && (date.getTime() - openOrder.getDate().getTime()) > orderTimeoutInHour * 1000 * 60 * 60) {
                openOrdersToCancel.add(openOrder);
            }
        }

        return openOrdersToCancel;
    }

    @Override
    public void cancelOrders(PoloniexTradingApi tradingApi, List<PoloniexOpenOrder> openOrderList, PolStrategy patienceStrategy, Date now) {

    }

    @Override
    public BigDecimal createOrders(BotUser user, PoloniexTradingApi tradingApi, BigDecimal btcBalance, List<PoloniexOrderResult> orderResults, List<Order> orders) {
        //fulfill orders
        for (Order order : orders) {
            if (order.getType().equalsIgnoreCase(PolBot.BUY_ACTION)) {

                PoloniexOrderResult result = createBuyOrder(user, tradingApi, order);
                orderResults.add(result);

                logger.debug("BUY Order {} created", order);

            } else {
                PoloniexOrderResult result = createSellOrder(user, tradingApi, order);
                orderResults.add(result);

                logger.debug("SELL Order {} created", order);
            }
        }
        return null;
    }


    public PoloniexOrderResult createSellOrder(BotUser user, PoloniexTradingApi tradingApi, Order order) {

        PoloniexOrderResult result = tradingApi.sell(order);

        SaveCurrencyTransaction(user, order.getTotal(), order, result);

        return result;
    }

    public PoloniexOrderResult createBuyOrder(BotUser user, PoloniexTradingApi tradingApi, Order order) {

        PoloniexOrderResult result = tradingApi.buy(order);

        SaveCurrencyTransaction(user, order.getTotal(), order, result);

        return result;
    }

    private void SaveCurrencyTransaction(BotUser user, BigDecimal budget, Order openOrder, PoloniexOrderResult result) {
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

}
