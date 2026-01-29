package com.project.demo.logic.entity.paypal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Setter
@Getter
public class PayPalRequest {

    List<BigInteger> publications;
}
