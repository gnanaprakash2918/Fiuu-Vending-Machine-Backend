package com.vendingmachine.vendingmachine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity // Marks this class as a JPA entity
@Table(name = "orders")
public class Order {
    @Id
    private String orderID;
    private String itemID;
    private double amount;
    private String status; // "PENDING", "PAID, "FAILED"
    @Column(length = 800)
    private String paymentURL; // Fiuu Payment URL
}
