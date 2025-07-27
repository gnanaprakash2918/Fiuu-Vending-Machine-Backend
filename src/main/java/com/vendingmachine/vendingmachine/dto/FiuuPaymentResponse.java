package com.vendingmachine.vendingmachine.dto;

import lombok.Data;

@Data
public class FiuuPaymentResponse {
    private String status;
    private String message;
    private String payment_url;
}
