package com.habanoz.polbot.core;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.model.PoloniexTradeHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class Main implements CommandLineRunner {
    @Autowired
    private PoloniexTradingApi tradingApi;

    @Autowired
    private PoloniexPublicApi publicApi;

    public static void main(String[] args) {

                SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        // Test comment to see github that commiting/pulling is working fine

        //Map<String, PoloniexTicker> tickerMap = publicApi.returnTicker();
        // for (String curPair:tickerMap.keySet())
        //    System.out.println(curPair);

        //Map<String, PoloniexCompleteBalance> balanceMap = tradingApi.returnCompleteBalances();
        //System.out.println(balanceMap.toString());

        Map<String, List<PoloniexTradeHistory>> balanceMap = tradingApi.returnTradeHistory();
        System.out.println(balanceMap.toString());

        //Map<String, Float> balancesMap = tradingApi.returnBalances();
        //System.out.println(balancesMap.toString());
        //System.out.println(tradingApi.returnBalances().toString());;
        //System.out.println(tradingApi.returnBalance("ETH").toString());
    }
}
