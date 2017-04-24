package com.habanoz.polbot.core.mail;

import com.habanoz.polbot.core.model.PoloniexCompleteBalance;
import com.habanoz.polbot.core.model.PoloniexOrderResult;
import com.habanoz.polbot.core.model.PoloniexTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

    public String getSummaryHTML(List<PoloniexOrderResult> orderResults, Map<String, List<PoloniexTrade>> recentHistoryMap, Map<String, PoloniexCompleteBalance> balancesMap) {

        List<PoloniexOrderResult> successful = orderResults.stream().filter(e -> e.getSuccess()).collect(Collectors.toList());
        List<PoloniexOrderResult> failed = orderResults.stream().filter(e -> !e.getSuccess()).collect(Collectors.toList());

        //pre process balance records
        Double btcBalance = balancesMap.values().stream().mapToDouble(PoloniexCompleteBalance::getBtcValue).sum();
        balancesMap = balancesMap.entrySet().stream().filter(map -> map.getValue().getBtcValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Context context = new Context();
        context.setVariable("recentHistoryMap", recentHistoryMap);
        context.setVariable("successfulOrders", successful);
        context.setVariable("failedOrders", failed);
        context.setVariable("balances", balancesMap);
        context.setVariable("btcBalance", btcBalance);

        return templateEngine.process("mail-operation-result", context);
    }

    public String getFailText(List<PoloniexOrderResult> orderResults, String str) {
        Context context = new Context();
        context.setVariable("orders", orderResults);
        context.setVariable("reason", str);

        return templateEngine.process("mail-operation-failed", context);
    }
}
