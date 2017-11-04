package com.habanoz.polbot.core.web.controller;

import com.habanoz.polbot.core.entity.BotTradeConfig;
import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.User;
import com.habanoz.polbot.core.repository.BotTradeConfigRepository;
import com.habanoz.polbot.core.repository.BotUserRepository;
import com.habanoz.polbot.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Date;
import java.util.Map;

/**
 * Created by huseyina on 4/9/2017.
 */
@Controller
public class MyBotTradeConfigController {

    @Autowired
    private BotTradeConfigRepository botTradeConfigRepository;

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping({"/mybottradeconfigs/{buid}"})
    public String welcome(@PathVariable("buid") Integer buid, Map<String, Object> model, Principal principal) {
        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        model.put("botUser", botUser);
        model.put("buid", buid);
        model.put("botTradeConfig", new BotTradeConfig());
        model.put("botTradeConfigs", this.botTradeConfigRepository.findByBotUserAndCompleted(botUser,0));

        return "mybottradeconfigs";
    }

    @RequestMapping(value = "/bottradeconfig", params = {"save"})
    public String saveBotTradeConfig(BotTradeConfig botTradeConfig, Principal principal, Map model, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "bottradeconfig";
        }

        if (botTradeConfig.getTradeConfigId()==null || botTradeConfig.getTradeConfigId()==0){
            botTradeConfig.setSellMode(0);
            botTradeConfig.setCompleted(0);
            botTradeConfig.setStatus("created");
            botTradeConfig.setCreated(new Date());
        }


        botTradeConfig.setUpdated(new Date());

        this.botTradeConfigRepository.save(botTradeConfig);

        model.clear();

        //return "redirect:/botTradeConfig?show=&currency=" + botTradeConfig.getCurrencyPair();
        return "redirect:/mybottradeconfigs/" + botTradeConfig.getBotUser().getBuId();
    }

    @RequestMapping(value = "/bottradeconfig", params = {"delete"})
    public String deleteBotTradeConfig(final BotTradeConfig botTradeConfig, final BindingResult bindingResult, final ModelMap model) {

        if (bindingResult.hasErrors()) {
            return "bottradeconfig";
        }

        this.botTradeConfigRepository.delete(botTradeConfig);
        model.clear();

        return "redirect:/mybottradeconfigs/" + botTradeConfig.getBotUser().getBuId();
    }

    @RequestMapping(value = "/bottradeconfig", params = {"back"})
    public String backBotTradeConfig(final BotTradeConfig botTradeConfig, final BindingResult bindingResult, final ModelMap model) {

        if (bindingResult.hasErrors()) {
            return "bottradeconfig";
        }

        return "redirect:/mybottradeconfigs/" + botTradeConfig.getBotUser().getBuId();
    }

    @RequestMapping(value = "/bottradeconfig/{buid}/{tradeConfigId}", params = {"show"})
    public String showBotTradeConfig(@PathVariable("tradeConfigId") Integer tradeConfigId,@PathVariable("buid") Integer buid, Principal principal, Map model) {

        User user = userRepository.findByUserName(principal.getName());
        BotUser botUser = botUserRepository.findByUserAndBuId(user, buid);

        BotTradeConfig currentbotTradeConfig = null;

        if (botUser != null && tradeConfigId!=null)
            currentbotTradeConfig = this.botTradeConfigRepository.findOne(tradeConfigId);

        if (currentbotTradeConfig == null)
            currentbotTradeConfig = new BotTradeConfig();

        if (currentbotTradeConfig.getBotUser() == null)
            currentbotTradeConfig.setBotUser(botUser);

        model.put("botUser", botUser);
        model.put("currency", currentbotTradeConfig .getCurrencyPair());
        model.put("botTradeConfig", currentbotTradeConfig);

        return "bottradeconfig";
    }

}
