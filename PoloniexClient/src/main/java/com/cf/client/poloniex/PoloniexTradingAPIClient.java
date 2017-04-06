package com.cf.client.poloniex;


import com.cf.TradingAPIClient;
import com.cf.client.HTTPClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.LogManager;

/**
 *
 * @author David
 */
public class PoloniexTradingAPIClient implements TradingAPIClient
{
    private static final String TRADING_URL = "https://poloniex.com/tradingApi?";
    private final String apiKey;
    private final String apiSecret;
    private final HTTPClient client;

    public PoloniexTradingAPIClient(String apiKey, String apiSecret)
    {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.client = new HTTPClient();
    }

    @Override
    public String returnBalances()
    {
        return this.returnTradingAPICommandResults("returnBalances");
    }

    @Override
    public String returnFeeInfo()
    {
        return this.returnTradingAPICommandResults("returnFeeInfo");
    }

    @Override
    public String returnCompleteBalances()
    {
        return this.returnTradingAPICommandResults("returnCompleteBalances");
    }

    @Override
    public String returnOpenOrders()
    {
        List<NameValuePair> additionalPostParams = new ArrayList<>();
        additionalPostParams.add(new BasicNameValuePair("currencyPair", "all"));
        return returnTradingAPICommandResults("returnOpenOrders", additionalPostParams);
    }

    @Override
    public String returnTradeHistory(String currencyPair)
    {
        List<NameValuePair> additionalPostParams = new ArrayList<>();
        additionalPostParams.add(new BasicNameValuePair("currencyPair", currencyPair == null ? "all" : currencyPair));
        additionalPostParams.add(new BasicNameValuePair("start", PoloniexExchangeService.LONG_LONG_AGO.toString()));
        return returnTradingAPICommandResults("returnTradeHistory", additionalPostParams);
    }

    @Override
    public String cancelOrder(String orderNumber)
    {
        List<NameValuePair> additionalPostParams = new ArrayList<>();
        additionalPostParams.add(new BasicNameValuePair("orderNumber", orderNumber));
        return returnTradingAPICommandResults("cancelOrder", additionalPostParams);
    }

    @Override
    public String moveOrder(String orderNumber, BigDecimal rate)
    {
        List<NameValuePair> additionalPostParams = new ArrayList<>();
        additionalPostParams.add(new BasicNameValuePair("orderNumber", orderNumber));
        additionalPostParams.add(new BasicNameValuePair("rate", rate.toPlainString()));
        additionalPostParams.add(new BasicNameValuePair("postOnly", "0"));
        additionalPostParams.add(new BasicNameValuePair("immediateOrCancel", "0"));
        return returnTradingAPICommandResults("moveOrder", additionalPostParams);
    }

    @Override
    public String sell(String currencyPair, BigDecimal sellPrice, BigDecimal amount, boolean fillOrKill, boolean immediateOrCancel, boolean postOnly)
    {
        return trade("sell", currencyPair, sellPrice, amount, fillOrKill, immediateOrCancel, postOnly);
    }

    @Override
    public String buy(String currencyPair, BigDecimal buyPrice, BigDecimal amount, boolean fillOrKill, boolean immediateOrCancel, boolean postOnly)
    {
        return trade("buy", currencyPair, buyPrice, amount, fillOrKill, immediateOrCancel, postOnly);
    }

    private String trade(String tradeType, String currencyPair, BigDecimal rate, BigDecimal amount, boolean fillOrKill, boolean immediateOrCancel, boolean postOnly)
    {
        List<NameValuePair> additionalPostParams = new ArrayList<>();
        additionalPostParams.add(new BasicNameValuePair("currencyPair", currencyPair));
        additionalPostParams.add(new BasicNameValuePair("rate", rate.toPlainString()));
        additionalPostParams.add(new BasicNameValuePair("amount", amount.toPlainString()));
        additionalPostParams.add(new BasicNameValuePair("fillOrKill", fillOrKill ? "1" : "0"));
        additionalPostParams.add(new BasicNameValuePair("immediateOrCancel", immediateOrCancel ? "1" : "0"));
        additionalPostParams.add(new BasicNameValuePair("postOnly", postOnly ? "1" : "0"));
        return returnTradingAPICommandResults(tradeType, additionalPostParams);
    }

    @Override
    public String returnTradingAPICommandResults(String commandValue, List<NameValuePair> additionalPostParams)
    {
        try
        {
            List<NameValuePair> postParams = new ArrayList<>();
            postParams.add(new BasicNameValuePair("command", commandValue));
            postParams.add(new BasicNameValuePair("nonce", String.valueOf(System.currentTimeMillis())));

            if (additionalPostParams != null && additionalPostParams.size() > 0)
            {
                postParams.addAll(additionalPostParams);
            }

            StringBuilder sb = new StringBuilder();
            for (NameValuePair postParam : postParams)
            {
                if (sb.length() > 0)
                {
                    sb.append("&");
                }
                sb.append(postParam.getName()).append("=").append(postParam.getValue());
            }
            String body = sb.toString();

            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(apiSecret.getBytes(), "HmacSHA512"));
            String signature = new String(Hex.encodeHex(mac.doFinal(body.getBytes())));

            List<NameValuePair> httpHeaders = new ArrayList<>();
            httpHeaders.add(new BasicNameValuePair("Key", apiKey));
            httpHeaders.add(new BasicNameValuePair("Sign", signature));

            return client.postHttp(TRADING_URL, postParams, httpHeaders);
        }
        catch (IOException | NoSuchAlgorithmException | InvalidKeyException ex)
        {
            LogManager.getLogger(PoloniexTradingAPIClient.class).warn("Call to Poloniex Trading API resulted in exception - " + ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    public String returnTradingAPICommandResults(String commandValue)
    {
        return returnTradingAPICommandResults(commandValue, new ArrayList<>());
    }

}
