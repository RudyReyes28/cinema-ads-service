package com.codenbugs.adsservice.dtos.ads;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class DeductRequest {
    BigDecimal amount;
}
