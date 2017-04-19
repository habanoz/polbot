package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.model.PoloniexOrderResult;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.model.PoloniexTradeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huseyina on 4/12/2017.
 */
@Component
public class HtmlHelper {

    @Autowired
    private TemplateEngine templateEngine;

    public String getHtmlText(List<PoloniexOrderResult> orderResults, List<PoloniexTradeResult> results, Map<String, List<PoloniexTrade>> recentHistoryMap) {

        List<PoloniexOrderResult> successful = orderResults.stream().filter(e -> e.getSuccess()).collect(Collectors.toList());
        List<PoloniexOrderResult> failed = orderResults.stream().filter(e -> !e.getSuccess()).collect(Collectors.toList());

        Context context = new Context();
        context.setVariable("recentHistoryMap", recentHistoryMap);
        context.setVariable("successfulOrders", successful);
        context.setVariable("failedOrders", failed);
        //TODO fix
        context.setVariable("trades", Collections.emptyList());

        return templateEngine.process("mail-operation-result", context);
    }

    public String getFailText(List<PoloniexOrderResult> orderResults, String str) {
        Context context = new Context();
        context.setVariable("orders", orderResults);
        context.setVariable("reason", str);

        return templateEngine.process("mail-operation-failed", context);
    }
}
