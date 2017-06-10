package com.habanoz.polbot.core.api;

import com.cf.TradingAPIClient;
import com.cf.client.poloniex.PoloniexExchangeService;
import com.cf.client.poloniex.PoloniexTradingAPIClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.model.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by habanoz on 02.04.2017.
 */
public class PoloniexTradingApiImpl implements PoloniexTradingApi {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTradingApiImpl.class);
    private static final Logger operationlogger = LoggerFactory.getLogger("PoloniexOperation");
    public static final String DATE_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    private TradingAPIClient tradingAPIClient;
    private ObjectMapper objectMapper;

    private BotUser botUser;

    public PoloniexTradingApiImpl(BotUser botUser) {
        this.botUser = botUser;
        tradingAPIClient = new PoloniexTradingAPIClient(botUser.getPublicKey(), botUser.getPrivateKey());

        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT_STR));

        module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);


        objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(module)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        DateFormat df = new SimpleDateFormat(DATE_FORMAT_STR);
        objectMapper.setDateFormat(df);
    }

    public BotUser getBotUser() {
        return botUser;
    }

    @Override
    public Map runCommand(String commandName, List<NameValuePair> params, TypeReference typeReference) {
        try {
            String json = tradingAPIClient.returnTradingAPICommandResults(commandName, params);
            if (json == null) return Collections.emptyMap();

            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            logger.error("Error while running command {}", commandName, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public PoloniexOrderResult buy(PoloniexOpenOrder order) {
        try {
            operationlogger.info("Attempting to order {}", order);

            String str = tradingAPIClient.buy(order.getCurrencyPair(), order.getRate(), order.getAmount(), false, false, false);

            if (str == null || str.contains("error")) {
                operationlogger.error("Failed order " + order.toString());
                return new PoloniexOrderResult(order, str);
            }

            PoloniexTradeResult result = objectMapper.readValue(str, PoloniexTradeResult.class);

            operationlogger.info("Buy resulted: {}", result.toString());
            return new PoloniexOrderResult(order, result);

        } catch (IOException e) {
            logger.error("Error at order {}", order, e);
            return new PoloniexOrderResult(order, e.getMessage());
        }
    }


    @Override
    public PoloniexOrderResult sell(PoloniexOpenOrder order) {

        try {
            operationlogger.info("Attempting to order {}", order);

            String str = tradingAPIClient.sell(order.getCurrencyPair(), order.getRate(), order.getAmount(), false, false, false);

            if (str == null || str.contains("error")) {
                operationlogger.error("Failed order " + order.toString());
                return new PoloniexOrderResult(order, str);
            }

            PoloniexTradeResult result = objectMapper.readValue(str, PoloniexTradeResult.class);

            operationlogger.info("Sell resulted: {}", result.toString());


            return new PoloniexOrderResult(order, result);

        } catch (IOException e) {
            logger.error("Error at order {}", order, e);
            return new PoloniexOrderResult(order, e.getMessage());
        }
    }

    @Override
    public Map runCommand(String commandName, TypeReference typeReference) {
        return runCommand(commandName, Collections.EMPTY_LIST, typeReference);
    }

    @Override
    public Map<String, List<PoloniexOpenOrder>> returnOpenOrders() {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("currencyPair", "all"));
        return runCommand("returnOpenOrders", params, new TypeReference<HashMap<String, List<PoloniexOpenOrder>>>() {
        });
    }

    @Override
    public Map<String, BigDecimal> returnBalances() {
        return runCommand("returnBalances", new TypeReference<HashMap<String, BigDecimal>>() {
        });
    }

    @Override
    public Map<String, PoloniexCompleteBalance> returnCompleteBalances() {
        Map<String, PoloniexCompleteBalance> completeBalanceMap = runCommand("returnCompleteBalances", new TypeReference<HashMap<String, PoloniexCompleteBalance>>() {
        });

        completeBalanceMap = completeBalanceMap.entrySet().stream().filter(map -> map.getValue().getBtcValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return completeBalanceMap;
    }

    @Override
    public BigDecimal returnBalance(String cur) {
        return returnBalances().get(cur);
    }

    @Override
    public Map<String, List<PoloniexTrade>> returnTradeHistory() {
        return returnTradeHistory(PoloniexExchangeService.LONG_LONG_AGO);
    }

    @Override
    public Map<String, List<PoloniexTrade>> returnTradeHistory(Long timeStampInSeconds) {

        List<NameValuePair> additionalPostParams = new ArrayList<>();
        additionalPostParams.add(new BasicNameValuePair("currencyPair", "all"));
        additionalPostParams.add(new BasicNameValuePair("start", timeStampInSeconds.toString()));
        //return runCommand("returnTradeHistory", additionalPostParams, new TypeReference<HashMap<String, List<PoloniexTrade>>>() {
        // });

        try {

            String result = tradingAPIClient.returnTradingAPICommandResults("returnTradeHistory", additionalPostParams);
            if (result == null) {
                logger.warn("returnTradeHistory command returned NULL");
                return Collections.emptyMap();
            }

            if (result.length() < 5) {//make sure long enough to contain trade data
                return Collections.emptyMap();
            }

            return objectMapper.readValue(result, new TypeReference<HashMap<String, List<PoloniexTrade>>>() {
            });
        } catch (IOException e) {
            logger.error("Error while running command {}", "returnTradeHistory", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean cancelOrder(String orderNumber) {
        List<NameValuePair> additionalPostParams = new ArrayList<>();
        additionalPostParams.add(new BasicNameValuePair("orderNumber", orderNumber));
        String result = tradingAPIClient.returnTradingAPICommandResults("cancelOrder", additionalPostParams);
        if (result != null && result.toLowerCase().contains("success"))
            return true;

        return false;
    }
}
