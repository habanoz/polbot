package com.habanoz.polbot.core.repository;

import com.habanoz.polbot.core.entity.OrderEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by huseyina on 4/7/2017.
 */

public interface OrderRepository extends CrudRepository<OrderEntity, String> {
}
