package com.vendingmachine.vendingmachine.Repository;

import com.vendingmachine.vendingmachine.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
}


