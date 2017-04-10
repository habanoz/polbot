package com.habanoz.polbot.core.mail;

/**
 * Created by huseyina on 4/9/2017.
 */
public interface MailService {
    void sendMail(String to,String header, String body);
}
