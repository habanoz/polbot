package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.model.PoloniexTradeResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by huseyina on 4/12/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class HtmlHelperTest {
    @Autowired
    private HtmlHelper htmlHelper;

    @Test
    public void getSuccessText() throws Exception {
        String html=htmlHelper.getSuccessText(new PoloniexOpenOrder("ETH_BTC", "BUY", new BigDecimal(0.12), new BigDecimal(2)), new PoloniexTradeResult(Arrays.asList(new PoloniexTrade[]{new PoloniexTrade(new BigDecimal(0.12), new BigDecimal(1.2), "BUY")})));
        System.out.println(html);
    }

    @Test
    public void getFailText() throws Exception {
        String html=htmlHelper.getFailText(new PoloniexOpenOrder("ETH_BTC", "BUY", new BigDecimal(0.12), new BigDecimal(2)), "<error>Error happened!<error>");
        System.out.println(html);
    }

}