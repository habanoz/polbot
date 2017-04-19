package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.TradeHistoryTrack;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by huseyina on 4/19/2017.
 */

public interface TradeHistoryTrackRepository extends JpaRepository<TradeHistoryTrack, Integer> {
}
