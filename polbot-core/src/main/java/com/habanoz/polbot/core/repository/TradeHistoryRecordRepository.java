package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.BotUser;
import com.habanoz.polbot.core.entity.TradeHistoryRecord;
import com.habanoz.polbot.core.entity.UserBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TradeHistoryRecordRepository
        extends JpaRepository<TradeHistoryRecord, Integer> {

}
