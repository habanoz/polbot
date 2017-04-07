package com.habanoz.polbot.core;

import com.habanoz.polbot.core.entity.OrderEntity;
import com.habanoz.polbot.core.model.PoloniexOpenOrder;
import com.habanoz.polbot.core.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.util.Date;

@SpringBootApplication
@EnableScheduling
public class Main implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Autowired
    OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        logger.info("Bot starting...");

        OrderEntity orderEntity=new OrderEntity();
        PoloniexOpenOrder poloniexOpenOrder=new PoloniexOpenOrder();
        poloniexOpenOrder.setAmount(new BigDecimal(5));
        poloniexOpenOrder.setOrderNumber("123123");
        poloniexOpenOrder.setRate(new BigDecimal(.5));
        orderEntity.setOrder(poloniexOpenOrder);
        orderEntity.setDateCreated(new Date());

        orderRepository.save(orderEntity);
    }
}
