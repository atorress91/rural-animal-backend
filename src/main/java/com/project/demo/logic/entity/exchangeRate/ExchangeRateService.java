package com.project.demo.logic.entity.exchangeRate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * servicio para obtener y calcular tasas de cambio,
 * esta clase interactúa con una API gratuita para recuperar las tasas
 * de cambio actuales y realiza cálculos para las tasas de compra y venta.
 */
@Slf4j
@Service
public class ExchangeRateService {

    private final WebClient webClient;

    @Value("${exchange.rate.api.url}")
    private String exchangeApiUrl;

    public ExchangeRateService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    /**
     * obtiene la tasa de cambio actual desde la API externa
     *
     * @return exchangeResponse que contiene la respuesta con las tasas de cambio procesadas
     */
    public Mono<ExchangeResponse> getExchangeRate() {
        return webClient.get()
                .uri(exchangeApiUrl)
                .retrieve()
                .bodyToMono(ExchangeRateApiResponse.class)
                .map(response -> {
                    double usdRate = response.getConversionRates().get("CRC");
                    log.info("USD a CRC: {}", usdRate);
                    return ExchangeResponse.builder()
                            .buy(roundToTwoDecimals(usdRate - 2))
                            .sell(roundToTwoDecimals(usdRate + 2))
                            .source("ExchangeRate-API")
                            .build();
                })
                .doOnSuccess(response -> log.info("Tipo de cambio consultado exitosamente: {}", response))
                .doOnError(e -> log.error("Error consultando el tipo de cambio: {}", e.getMessage()))
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * redondea un valor a dos decimales.
     *
     * @param value el valor a redondear.
     * @return el valor redondeado a dos decimales.
     */
    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}