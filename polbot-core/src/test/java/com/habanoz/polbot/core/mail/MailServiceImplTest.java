package com.habanoz.polbot.core.mail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by huseyina on 4/9/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MailServiceImplTest {

    @Autowired
    MailService mailService;
    @Test
    public void sendMail() throws Exception {
        mailService.sendMail("merab","naber");
    }

}