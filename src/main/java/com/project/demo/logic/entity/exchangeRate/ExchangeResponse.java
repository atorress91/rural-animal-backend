package com.project.demo.logic.entity.exchangeRate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * clase que representa la respuesta de las tasas de cambio.
 * contiene las tasas de compra, venta, la fecha de consulta
 * y la fuente de los datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponse {

    /**
     * tasa de cambio para la compra.
     */
    private double buy;

    /**
     * tasa de cambio para la venta.
     */
    private double sell;

    /**
     * fecha y hora de la consulta de las tasas de cambio.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime queryDate = LocalDateTime.now();

    /**
     * fuente de los datos de la tasa de cambio.
     * Por defecto, se establece en "External API".
     */
    @Builder.Default
    private String source = "External API";
}