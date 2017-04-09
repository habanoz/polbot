package com.habanoz.polbot.core.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Created by huseyina on 4/9/2017.
 */
@Service
public class MailServiceImpl implements MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender javaMailSender;

    private String to;
    private String from;


    public MailServiceImpl(@Value("${spring.mail.from}") String from, @Value("${spring.mail.to}") String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void sendMail(String header, String body) {
        MimeMessage mail = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(to);
            helper.setReplyTo(to);
            helper.setFrom(from);
            helper.setSubject(header);
            helper.setText(body);

            javaMailSender.send(mail);
        } catch (MessagingException e) {
            logger.error("Error while sending mail", e);
        }
    }
}