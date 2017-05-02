package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by huseyina on 4/12/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class HtmlHelperTest {
    @Autowired
    private HtmlHelper htmlHelper;

    @Autowired
    MailService mailService;


    @Test
    public void testSummaryMail() {
        List<PoloniexOrderResult> orderResults = new ArrayList<>();
        orderResults.add(new PoloniexOrderResult(new PoloniexOpenOrder("BTC_ETH", "BUY", new BigDecimal(0.1), new BigDecimal(5)), new PoloniexTradeResult()));
        orderResults.add(new PoloniexOrderResult(new PoloniexOpenOrder("BTC_ETC", "SELL", new BigDecimal(0.2), new BigDecimal(2)), new PoloniexTradeResult()));
        orderResults.add(new PoloniexOrderResult(new PoloniexOpenOrder("BTC_ETC", "BUY", new BigDecimal(0.2), new BigDecimal(2)), "Not enough BTC"));


        Map<String, List<PoloniexTrade>> recentTrades = new HashMap<>();
        recentTrades.put("ETC", Arrays.asList(new PoloniexTrade(new BigDecimal(0.23), new BigDecimal(2), "BUY"), new PoloniexTrade(new BigDecimal(0.23), new BigDecimal(2), "BUY")));
        recentTrades.put("ETH", Collections.singletonList(new PoloniexTrade(new BigDecimal(0.23), new BigDecimal(2), "BUY")));

        Map<String, PoloniexCompleteBalance> completeBalanceMap = new HashMap<>();
        completeBalanceMap.put("ETC", new PoloniexCompleteBalance(0.1f, 0.1f, 0.002f));
        completeBalanceMap.put("ETH", new PoloniexCompleteBalance(0.2f, 0.1f, 0.012f));
        completeBalanceMap.put("XMR", new PoloniexCompleteBalance(0.345f, 0.1f, 0.00014f));

        mailService.sendMail("eminyuce@gmail.com", "Orders Given", htmlHelper.getSummaryHTML(orderResults, recentTrades, completeBalanceMap), true);
    }

    @Test
    public void testSortedBalances() {
        Map<String,PoloniexCompleteBalance> map=new HashMap<>();
        map.put("ETC",new PoloniexCompleteBalance(0.1f,0.1f,0.4f));
        map.put("ETH",new PoloniexCompleteBalance(0.1f,0.1f,0.1f));
        map.put("LTC",new PoloniexCompleteBalance(0.1f,0.1f,0.2f));
        map.put("XMR",new PoloniexCompleteBalance(0.1f,0.1f,0.21f));
        map.put("XRP",new PoloniexCompleteBalance(0.1f,0.1f,0.02f));
        Map<String,PoloniexCompleteBalance> sortedMap=htmlHelper.getSortedBalances(map);

        for (String key:sortedMap.keySet()){
            System.out.println(key);
        }
    }


}