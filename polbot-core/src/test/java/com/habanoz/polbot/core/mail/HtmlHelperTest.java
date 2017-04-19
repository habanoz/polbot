package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexOrderResult;
import com.habanoz.polbot.core.model.PoloniexTradeResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<PoloniexTradeResult> tradeResults = new ArrayList<>();
        tradeResults.add(new PoloniexTradeResult(Collections.emptyList()));

        mailService.sendMail("huseyinabanox@gmail.com", "Orders Given", htmlHelper.getHtmlText(orderResults, tradeResults, recentHistoryMap), true);
    }
}