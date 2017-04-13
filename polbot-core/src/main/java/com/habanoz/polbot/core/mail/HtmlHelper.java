package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexTradeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

/**
 * Created by huseyina on 4/12/2017.
 */
@Component
public class HtmlHelper {

    @Autowired
    private TemplateEngine templateEngine;

    public String getSuccessText(PoloniexOpenOrder order, PoloniexTradeResult result) {
        Context context = new Context();
        context.setVariable("orders", Collections.singleton(order));
        context.setVariable("trades", result.getResultingTrades());

        return templateEngine.process("mail-operation-successful", context);
    }

    public String getFailText(PoloniexOpenOrder order, String str) {
        Context context = new Context();
        context.setVariable("orders", Collections.singleton(order));
        context.setVariable("reason", str);

        return templateEngine.process("mail-operation-failed", context);
    }
}
