package fiuu;

import lombok.Data;

@Data
public class FiuuCallbackRequest {
    private String orderID;
    private String amount;
    private String status; // "PAID", "FAILED", "PENDING"
}