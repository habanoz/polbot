package com.habanoz.polbot.core.web.controller;

import com.cf.TradingAPIClient;
import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.CurrencyConfig;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.CurrencyConfigRepository;
import com.habanoz.polbot.core.service.IAuthenticationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by Yuce on 4/17/2017.
 */
@Controller
public class OrdersController {


    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);



    @Autowired
    private CurrencyConfigRepository currencyConfigRepository;

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    private static final long BUY_SELL_SLEEP = 100;


//    @RequestMapping(value = "/orders/predefinedorders")
//    public String welcome(Map<String, Object> model) {
//        return "predefinedorders";
//    }
//    @RequestMapping(value = "/orders/cancelallorders")
//    public String cancelallorders(Map<String, Object> model) {
//        int userId=1;  //Authenticated User
//        Map<String, List<PoloniexOpenOrder>> openOrderMap = getOpenOrdersList(userId);
//
//        return "predefinedorders";
//    }

    //Develop poloneix open orders page to manage any currency cancellation,  any of them or all of them together.
    @RequestMapping(value = "/orders/openorders")
    public String openOrders(Map<String, Object> model) {
        int userId= authenticationFacade.GetUserId();

        Map<String, List<PoloniexOpenOrder>> openOrderMap = getOpenOrdersList(userId);


        model.put("userOpenOrders", openOrderMap);

        return "openOrders";
    }
    @RequestMapping(value = "/orders/cancelopenorders", method = RequestMethod.GET)
    public String cancelopenorders(@RequestParam("ordercanceltype") String orderCancelType) {
        int userId= authenticationFacade.GetUserId();
        BotUser user = botUserRepository.findOne(userId);
        Map<String, List<PoloniexOpenOrder>> openOrderMap = getOpenOrdersList(userId);
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(user);
        //let spring autowire marked attributes
        applicationContext.getAutowireCapableBeanFactory().autowireBean(tradingApi);

        //String orderCancelType="BUY";  // ALL,BUY,SELL
        for(Map.Entry<String, List<PoloniexOpenOrder>> mapKey : openOrderMap.entrySet()) {
            String key = mapKey.getKey();
            List<PoloniexOpenOrder> ordersForEachCurrency = mapKey.getValue();
            for (PoloniexOpenOrder order:  ordersForEachCurrency) {
                try {
                    if( orderCancelType.equalsIgnoreCase("ALL")){
                        boolean isResult = tradingApi.cancelOrder(order.getOrderNumber());
                    }
                    else if(order.getType().equalsIgnoreCase("BUY") && orderCancelType.equalsIgnoreCase("BUY")){
                        boolean isResult = tradingApi.cancelOrder(order.getOrderNumber());
                    }
                    else if(order.getType().equalsIgnoreCase("SELL") && orderCancelType.equalsIgnoreCase("SELL")){
                        boolean isResult = tradingApi.cancelOrder(order.getOrderNumber());
                    }

                    Thread.sleep(BUY_SELL_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return "redirect:/orders/openorders";
    }
    @RequestMapping(value = "/orders/stopcurrencyoperations", method = RequestMethod.GET)
    public String stopCurrencyOperations(@RequestParam("ordertype") String orderType) {
        int userId= authenticationFacade.GetUserId();
        List<CurrencyConfig> userCurrencyConfigs = currencyConfigRepository.findByUserId(userId);
        for (CurrencyConfig config: userCurrencyConfigs) {
            if(orderType.equalsIgnoreCase("STOP_BUY")){
                config.setBuyable(false);
            }else if(orderType.equalsIgnoreCase("STOP_SELL")) {
                config.setSellable(false);
            }else if(orderType.equalsIgnoreCase("START_BUY")) {
                config.setBuyable(true);
            }else if(orderType.equalsIgnoreCase("START_SELL")) {
                config.setSellable(true);
            }
            currencyConfigRepository.save(config);
        }

        return "redirect:/orders/openorders";
    }
    private Map<String, List<PoloniexOpenOrder>> getOpenOrdersList(int userId) {
        BotUser user = botUserRepository.findOne(userId);
        //create tradingApi instance for current user
        PoloniexTradingApi tradingApi = new PoloniexTradingApiImpl(user);
        //let spring autowire marked attributes
        applicationContext.getAutowireCapableBeanFactory().autowireBean(tradingApi);

        Map<String, BigDecimal> balanceMap = tradingApi.returnBalances();

        Map<String, List<PoloniexOpenOrder>> openOrderMap = tradingApi.returnOpenOrders();
        openOrderMap.values().removeAll(openOrderMap.values().stream().filter(r->r.size() == 0).collect(Collectors.toList()));
        return openOrderMap;
    }

}
