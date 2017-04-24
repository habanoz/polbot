package com.habanoz.polbot.core.robot;

import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.UserBot;
import com.habanoz.polbot.core.mail.HtmlHelper;
import com.habanoz.polbot.core.mail.MailService;
import com.habanoz.polbot.core.model.PoloniexTrade;
import com.habanoz.polbot.core.repository.TradeHistoryTrackRepository;
import com.habanoz.polbot.core.repository.UserBotRepository;
import com.habanoz.polbot.core.service.TradeTrackerService;
import com.habanoz.polbot.core.service.TradeTrackerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by habanoz on 22.04.2017.
 */
@Component
public class PoloniexTradeTrackerBot {
    private static final Logger logger = LoggerFactory.getLogger(PoloniexTradeTrackerBot.class);

    @Autowired
    private UserBotRepository userBotRepository;

    @Autowired
    private TradeHistoryTrackRepository tradeHistoryTrackRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private HtmlHelper htmlHelper;

    public PoloniexTradeTrackerBot() {

    }

    @PostConstruct
    public void init() {
    }


    @Scheduled(fixedDelay = 300000)
    public void runLogic() {


        logger.info("Bot started");

        List<BotUser> activeBotUsers = userBotRepository.findByBot(getClass().getSimpleName()).stream().map(UserBot::getUser).collect(Collectors.toList());
        for (BotUser user : activeBotUsers) {
            startTradingForEachUser(user);
        }

        logger.info("Bot completed");
    }

    private void startTradingForEachUser(BotUser user) {

        PoloniexTradingApi poloniexTradingApi = new PoloniexTradingApiImpl(user);

        TradeTrackerService tradeTrackerService = new TradeTrackerServiceImpl(tradeHistoryTrackRepository, poloniexTradingApi, user);

        Map<String, List<PoloniexTrade>> recentTrades = tradeTrackerService.returnTrades(true);

        if (!recentTrades.isEmpty())
            logger.info("Recent trades found for user {}", user);


        if (!recentTrades.isEmpty())// if any of them is not empty send mail
            mailService.sendMail(user.getUserEmail(), "Recent Trades", htmlHelper.getSummaryHTML(Collections.EMPTY_LIST, recentTrades,poloniexTradingApi.returnCompleteBalances()), true);

    }
}
