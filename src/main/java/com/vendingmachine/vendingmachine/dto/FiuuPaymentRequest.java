package com.vendingmachine.vendingmachine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FiuuPaymentRequest {
    private String merchantID;
    private String orderID;
    private String amount;
    private String vcode;
    private String skey;
    private String channel;
}
