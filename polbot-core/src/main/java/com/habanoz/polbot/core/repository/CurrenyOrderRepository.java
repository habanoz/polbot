package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.CurrenyOrder;
import com.habanoz.polbot.core.entity.UserBot;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Yuce on 4/27/2017.
 */
public interface CurrenyOrderRepository  extends JpaRepository<CurrenyOrder, Integer> {

}
