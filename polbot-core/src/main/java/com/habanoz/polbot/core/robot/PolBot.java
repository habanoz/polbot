package com.habanoz.polbot.core.robot;

/**
 * Created by huseyina on 5/29/2017.
 */
public interface PolBot {
    String BUY_ACTION = "BUY";
    String SELL_ACTION = "SELL";

    void execute();
}
