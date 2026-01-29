package com.project.demo.logic.entity.paypal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PayPalResponse {

    private String paymentId;
    private String status;
    private String amount;
    private String currency;

    public PayPalResponse(String paymentId, String status, String amount, String currency) {
        this.paymentId = paymentId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
    }
}