package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyCollectiveOrder;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.entity.User;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexOrderResult;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.registry.PublicPoloniexTickerRegistry;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by Yuce on 4/17/2017.
 */
@Controller
public class OrdersController {


    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private static final String CURR_PAIR_SEPARATOR = "_";

    @Autowired
    private PublicPoloniexTickerRegistry publicRegistry;

    @Autowired
    private PoloniexPublicApi publicApi;

    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private static final long BUY_SELL_SLEEP = 100;


    //Develop poloneix open orders page to manage any currency cancellation,  any of them or all of them together.
    @RequestMapping(value = "/orders/openorders/{buid}")
    public String openOrders(Map<String, Object> model, Principal principal, @PathVariable("buid") Integer buid) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        Map<String, List<PoloniexOpenOrder>> openOrderMap = getOpenOrdersList(botUser);

        model.put("userOpenOrders", openOrderMap);

        return "openOrders";
    }

    @RequestMapping(value = "/orders/stopcurrencyoperations/{buid}", method = RequestMethod.GET)
    public String stopCurrencyOperations(@RequestParam("ordertype") String orderType, @PathVariable("buid") Integer buid, Principal principal) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        List<CurrencyConfig> userCurrencyConfigs = currencyConfigRepository.findByBotUser(botUser);
        for (CurrencyConfig config : userCurrencyConfigs) {
            if (orderType.equalsIgnoreCase("STOP_BUY")) {
                config.setBuyable(false);
            } else if (orderType.equalsIgnoreCase("STOP_SELL")) {
                config.setSellable(false);
            } else if (orderType.equalsIgnoreCase("START_BUY")) {
                config.setBuyable(true);
            } else if (orderType.equalsIgnoreCase("START_SELL")) {
                config.setSellable(true);
            }
            currencyConfigRepository.save(config);
        }

        return "redirect:/orders/openorders/" + buid;
    }

    @RequestMapping(value = "/orders/cancelopenorders/{buid}", method = RequestMethod.GET)
    public String cancelopenorders(@RequestParam("ordercanceltype") String orderCancelType, @PathVariable("buid") Integer buid, Principal principal) {

        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        Map<String, List<PoloniexOpenOrder>> openOrderMap = getOpenOrdersList(botUser);
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(botUser);
        //let spring autowire marked attributes
        applicationContext.getAutowireCapableBeanFactory().autowireBean(tradingApi);

        //String orderCancelType="BUY";  // ALL,BUY,SELL
        for (Map.Entry<String, List<PoloniexOpenOrder>> mapKey : openOrderMap.entrySet()) {
            String key = mapKey.getKey();
            List<PoloniexOpenOrder> ordersForEachCurrency = mapKey.getValue();
            for (PoloniexOpenOrder order : ordersForEachCurrency) {
                try {
                    if (orderCancelType.equalsIgnoreCase("ALL")) {
                        boolean isResult = tradingApi.cancelOrder(order.getOrderNumber());
                    } else if (order.getType().equalsIgnoreCase("BUY") && orderCancelType.equalsIgnoreCase("BUY")) {
                        boolean isResult = tradingApi.cancelOrder(order.getOrderNumber());
                    } else if (order.getType().equalsIgnoreCase("SELL") && orderCancelType.equalsIgnoreCase("SELL")) {
                        boolean isResult = tradingApi.cancelOrder(order.getOrderNumber());
                    }

                    Thread.sleep(BUY_SELL_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return "redirect:/orders/openorders/" + buid;
    }

    @RequestMapping(value = "/orders/stopbuyordersforcurrencies/{buid}", method = RequestMethod.GET)
    public String stopbuyordersforcurrencies(@PathVariable("buid") Integer buid, Principal principal) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        Map<String, List<PoloniexOpenOrder>> openOrderMap = getOpenOrdersList(botUser);
        List<CurrencyConfig> userCurrencyConfigs = currencyConfigRepository.findByBotUser(botUser);

        for (CurrencyConfig config : userCurrencyConfigs) {
            try {

                String currPair = config.getCurrencyPair();
                //String currName = currPair.split(CURR_PAIR_SEPARATOR)[1];
                List<PoloniexOpenOrder> openOrders = openOrderMap.get(currPair);
                if (openOrders.stream().filter(r -> r.getType().equalsIgnoreCase("SELL")).count() >= 3) {
                    config.setBuyable(false);
                    currencyConfigRepository.save(config);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "redirect:/orders/openorders/" + buid;
    }


    private Map<String, List<PoloniexOpenOrder>> getOpenOrdersList(BotUser botUser) {

        //create tradingApi instance for current user
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(botUser);
        //let spring autowire marked attributes
        applicationContext.getAutowireCapableBeanFactory().autowireBean(tradingApi);

        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();

        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();
        openOrderMap.values().removeAll(openOrderMap.values().stream().filter(r -> r.size() == 0).collect(Collectors.toList()));
        return openOrderMap;
    }


    @RequestMapping(value = "/orders/savePercentageForAllCurrencies/{buid}")
    public String savePercentageForAllCurrencies(final CurrencyConfig currencyConfig, Principal principal, @PathVariable("buid") Integer buid) {


        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        List<CurrencyConfig> userCurrencyConfigs = currencyConfigRepository.findByBotUser(botUser);

        for (CurrencyConfig config : userCurrencyConfigs) {

            if (currencyConfig.getUsableBalancePercent() > 0) {
                config.setUsableBalancePercent(currencyConfig.getUsableBalancePercent());
            }
            if (currencyConfig.getSellOnPercent() > 0)
                config.setSellOnPercent(currencyConfig.getSellOnPercent());

            if (currencyConfig.getBuyOnPercent() > 0)
                config.setBuyOnPercent(currencyConfig.getBuyOnPercent());

            currencyConfigRepository.save(config);
        }

        return "redirect:/orders/openorders/" + buid;
    }

    @RequestMapping(value = "/orders/addMissingCurrencies/{buid}")
    public String addMissingCurrencies(Principal principal, final Map model, @PathVariable("buid") Integer buid) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        List<CurrencyConfig> userCurrencyConfigs = currencyConfigRepository.findByBotUser(botUser);

        Map<String, PoloniexTicker> tickers = publicApi.returnTicker();
        for (Map.Entry<String, PoloniexTicker> entry : tickers.entrySet()) {
            List<CurrencyConfig> c = userCurrencyConfigs.stream().filter(r -> r.getCurrencyPair().equals((entry.getKey()))).collect(Collectors.toList());

            if (c.size() == 0) {
                System.out.println(entry.getKey());
                CurrencyConfig currencyConfig = new CurrencyConfig();
                currencyConfig.setBuyable(false);
                currencyConfig.setBuyOnPercent(10);
                currencyConfig.setBuyAtPrice(0);
                currencyConfig.setSellable(false);
                currencyConfig.setSellAtPrice(0);
                currencyConfig.setSellOnPercent(10);
                currencyConfig.setCurrencyPair(entry.getKey());
                currencyConfig.setBotUser(botUser);
                currencyConfigRepository.save(currencyConfig);
            }
        }

        return "redirect:/orders/openorders/" + botUser.getBuId();
    }


    @RequestMapping(value = "/orders/setPercentageForAllCurrencies/{buid}")
    public String setPercentageForAllCurrencies(Principal principal, Map model, @PathVariable("buid") Integer buid) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        CurrencyConfig currentCurrencyConfig = new CurrencyConfig();
        currentCurrencyConfig.setBotUser(botUser);

        model.put("currencyConfig", currentCurrencyConfig);

        return "currencyconfigforall";
    }

    @RequestMapping(value = "/orders/collectiveOrders/{buid}/{currencyPair}", params = {"getorders"})
    public String collectiveOrders(Principal principal, Map model, @PathVariable("buid") Integer buid, @PathVariable("currencyPair") String currencyPair) {

        CurrencyCollectiveOrder collectiveCurrencyOrder = new CurrencyCollectiveOrder();
        PoloniexTicker poloniexTicker = publicRegistry.getTickerMap().getTickerMap().get(currencyPair);


        collectiveCurrencyOrder.setCurrencyPair(currencyPair);
        collectiveCurrencyOrder.setOrderType("BUY");
        if (poloniexTicker != null) {
            BigDecimal lowestAsk = poloniexTicker.getLowestAsk();
            collectiveCurrencyOrder.setTopPriceStr(lowestAsk.toString());
            collectiveCurrencyOrder.setBottomPriceStr(lowestAsk.toString());
        }

        collectiveCurrencyOrder.setPriceSplitter(5);
        collectiveCurrencyOrder.setTotalBtcAmount(0.1);

        model.put("collectiveCurrencyOrder", collectiveCurrencyOrder);
        model.put("buid", buid);

        return "collectiveorder";
    }

    @RequestMapping(value = "/orders/collectiveOrders/{buid}", params = {"startorder"})
    public String collectiveOrders(Principal principal, final CurrencyCollectiveOrder collectiveCurrencyOrder, @PathVariable("buid") Integer buid) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(botUser);

        collectiveOrders(collectiveCurrencyOrder, tradingApi);

        return "redirect:/orders/openorders/" + buid;
    }

    private void collectiveOrders(CurrencyCollectiveOrder collectiveCurrencyOrder, PoloniexTradingApi tradingApi) {

        if (collectiveCurrencyOrder.getPriceSplitter() > 0 && collectiveCurrencyOrder.getTotalBtcAmount() > 0) {
            BigDecimal topPrice = new BigDecimal(collectiveCurrencyOrder.getTopPriceStr());
            BigDecimal bottomPrice = new BigDecimal(collectiveCurrencyOrder.getBottomPriceStr());

            double btcAmountForEachIteration = collectiveCurrencyOrder.getTotalBtcAmount() / collectiveCurrencyOrder.getPriceSplitter();
            BigDecimal priceIncreaseForEachIteration = (topPrice.subtract(bottomPrice)).divide(BigDecimal.valueOf(collectiveCurrencyOrder.getPriceSplitter()));

            for (int i = 0; i < collectiveCurrencyOrder.getPriceSplitter(); i++) {
                BigDecimal priceForEachIteration = bottomPrice.add(priceIncreaseForEachIteration.multiply(BigDecimal.valueOf(i)));

                if (collectiveCurrencyOrder.getTotalBtcAmount() > 0) {
                    try {
                        System.out.println(i + ")  Price Of Order: " + priceForEachIteration + " Amount of BTC " + btcAmountForEachIteration);

                        String orderType = collectiveCurrencyOrder.getOrderType();
                        String currPair = collectiveCurrencyOrder.getCurrencyPair();

                        if (collectiveCurrencyOrder.getOrderType().equalsIgnoreCase("BUY")) {
                            BigDecimal buyBudget = new BigDecimal(btcAmountForEachIteration);
                            // buying price should be a little lower to make profit
                            // if set, buy at price will be used, other wise buy on percent will be used
                            BigDecimal buyPrice = priceForEachIteration;
                            // calculate amount that can be bought with buyBudget and buyPrice
                            BigDecimal buyAmount = buyBudget.divide(buyPrice, RoundingMode.HALF_UP);
                            PoloniexOpenOrder openOrder = new PoloniexOpenOrder(currPair, orderType, buyPrice, buyAmount);
                            logger.info("Attempted to {}", openOrder);
                            PoloniexOrderResult result = tradingApi.buy(openOrder);
                            logger.info("Buy Result:{}", result);

                        } else if (collectiveCurrencyOrder.getOrderType().equalsIgnoreCase("SELL")) {
                            BigDecimal sellPrice = priceForEachIteration;
                            BigDecimal sellAmount = new BigDecimal(btcAmountForEachIteration).divide(sellPrice,BigDecimal.ROUND_HALF_DOWN);

                            PoloniexOpenOrder openOrder = new PoloniexOpenOrder(currPair, orderType, sellPrice, sellAmount);
                            logger.info("Attempted to {}", openOrder);

                            PoloniexOrderResult result = tradingApi.sell(openOrder);

                            logger.info("Sell Result:{}", result);
                        }


                    } catch (Exception ex) {
                        logger.error("Error at bulk order ", ex);
                    }
                    sleep();

                }
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(BUY_SELL_SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
