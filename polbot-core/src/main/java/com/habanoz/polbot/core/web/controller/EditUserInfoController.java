package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.api.PoloniexTradingApi;
import com.habanoz.polbot.core.api.PoloniexTradingApiImpl;
import com.habanoz.polbot.core.entity.*;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.model.PoloniexOrderResult;
import com.habanoz.polbot.core.model.PoloniexTicker;
import com.habanoz.polbot.core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by Yuce on 4/17/2017.
 */
@Controller
public class EditUserInfoController {
    private static final Logger logger = LoggerFactory.getLogger(EditUserInfoController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private UserBotRepository userBotRepository;

    @RequestMapping(value = "/editbotuserinfo/{buid}", params = {"show"})
    public String showEditBotUserinfo(Principal principal, Map model, @PathVariable("buid") Integer buid) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        model.put("botuser", botUser);

        return "editbotuserinfo";
    }

    @RequestMapping(value = "/editbotuserinfo", params = {"save"})
    public String saveEditBotUserinfo(Principal principal, final BotUser botuser, Map model) {
        // User user = userRepository.findByUserName(principal.getName());
        //BotUser botUser = botUserRepository.findByUserAndBuId(user, index);

        botUserRepository.save(botuser);

        return "redirect:/editbotuserinfo/" + botuser.getBuId() + "?show=";
    }

    @RequestMapping(value = "/editbotinfo/{buid}", params = {"show"})
    public String showEditBotInfo(Principal principal, Map model, @PathVariable("buid") Integer buid) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);
        List<UserBot> userBots = userBotRepository.findByUser(botUser);

        model.put("bots", botRepository.findAll());
        model.put("userBot", userBots.get(0));

        return "editbotinfo";
    }

    @RequestMapping(value = "/editbotinfo", params = {"save"})
    public String showEditBotInfo(Principal principal, final UserBot userBot, Map model) {
        // User user = userRepository.findByUserName(principal.getName());
        //BotUser botUser = botUserRepository.findByUserAndBuId(user, index);

        userBotRepository.save(userBot);

        return "redirect:/editbotinfo/" + userBot.getUser().getBuId() + "?show=";
    }

    @RequestMapping(value = "/edituserinfo", params = {"show"})
    public String showEditUserinfo(Principal principal, Map model) {
        User user = userRepository.findByUserName(principal.getName());

        model.put("user", user);

        return "edituserinfo";
    }

    @RequestMapping(value = "/edituserinfo", params = {"save"})
    public String saveEditUserinfo(Principal principal, final User user, Map model) {
        // User user = userRepository.findByUserName(principal.getName());
        //BotUser botUser = botUserRepository.findByUserAndBuId(user, index);

        userRepository.save(user);

        return "redirect:/edituserinfo?show=";
    }
}
