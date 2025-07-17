package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderID;
    private String itemID;
    private double amount;
    private String status; // "PENDING", "PAID, "FAILED"
    private String paymentURL; // Fiuu Payment URL
}
