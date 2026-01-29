package com.project.demo.logic.entity.exchangeRate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * clase que representa la respuesta de la API de tasas de cambio,
 * esta clase se mapea directamente a los datos JSON proporcionados
 * por la API externa.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateApiResponse {

    /**
     * estado del resultado de la solicitud (por ejemplo, "success")
     */
    private String result;

    /**
     * URL de la documentación de la API
     */
    private String documentation;

    /**
     * términos de uso de la API
     */
    @JsonProperty("terms_of_use")
    private String termsOfUse;

    /**
     * marca de tiempo (en formato UNIX) de la última actualización de las tasas
     */
    @JsonProperty("time_last_update_unix")
    private Long timeLastUpdateUnix;

    /**
     * fecha y hora de la última actualización de las tasas en formato UTC
     */
    @JsonProperty("time_last_update_utc")
    private String timeLastUpdateUtc;

    /**
     * marca de tiempo (en formato UNIX) para la próxima actualización de las tasas
     */
    @JsonProperty("time_next_update_unix")
    private Long timeNextUpdateUnix;

    /**
     * fecha y hora de la próxima actualización de las tasas en formato UTC
     */
    @JsonProperty("time_next_update_utc")
    private String timeNextUpdateUtc;

    /**
     * código base de la moneda utilizada como referencia (por ejemplo, "USD")
     */
    @JsonProperty("base_code")
    private String baseCode;

    /**
     * mapa de tasas de conversión donde la clave es el código de la moneda
     * y el valor es la tasa de conversión correspondiente
     */
    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;
}