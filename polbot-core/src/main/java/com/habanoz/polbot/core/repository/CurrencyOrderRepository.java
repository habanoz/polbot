package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.CurrencyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by Yuce on 4/27/2017.
 */
public interface CurrencyOrderRepository extends JpaRepository<CurrencyOrder, Integer> {
    List<CurrencyOrder> findByUserIdAndIsActive(Integer userId, boolean isActive);
    CurrencyOrder findByUserIdAndOrderNumber(Integer userId, String orderNumber);
    List<CurrencyOrder> findByUserIdAndIsActiveAndOrderDateGreaterThanOrderDateByOrderDateAsc(Integer userId, boolean isActive, Date orderDate);

}
