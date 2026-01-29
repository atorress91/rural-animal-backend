package com.project.demo.logic.entity.paypal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayPalSettings {
    Double total;
    String currency;
    String method;
    String intent;
    String description;
    String cancelUrl;
    String successUrl;

    public PayPalSettings(Double total, String currency, String method, String intent, String description, String cancelUrl, String successUrl) {
        this.total = total;
        this.currency = currency;
        this.method = method;
        this.intent = intent;
        this.description = description;
        this.cancelUrl = cancelUrl;
        this.successUrl = successUrl;
    }
}
