// OrderRepository.java
package com.vendingmachine.vendingmachine.Repository;

import com.vendingmachine.vendingmachine.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}