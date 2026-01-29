package com.project.demo.logic.entity.paypal;

import com.paypal.api.payments.*;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.project.demo.logic.entity.exchangeRate.ExchangeRateService;
import com.project.demo.logic.entity.exchangeRate.ExchangeResponse;
import com.project.demo.logic.entity.transaction.TblTransaction;
import com.project.demo.logic.entity.transaction.TblTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.project.demo.logic.entity.publication.TblPublication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * servicio que gestiona la integración con paypal para la creación y ejecución de pagos
 */
@Slf4j
@Service
public class PayPalService {

    private final APIContext apiContext;
    private final TblTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;

    /**
     * constructor de paypal service
     *
     * @param apiContext            contexto de la api de paypal utilizado para autenticar las solicitudes
     * @param transactionRepository repositorio para gestionar las transacciones en la base de datos
     */
    public PayPalService(APIContext apiContext, TblTransactionRepository transactionRepository, ExchangeRateService exchangeRateService) {
        this.apiContext = apiContext;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * crea un nuevo pago en paypal basado en una transacción
     *
     * @param transaction la transacción que contiene los detalles del pago
     * @return el objeto payment creado en paypal
     * @throws PayPalRESTException si ocurre un error al crear el pago en paypal
     */
    public Payment createPayment(TblTransaction transaction) throws PayPalRESTException {
        try {

            ExchangeResponse exchangeRate = exchangeRateService.getExchangeRate().block();
            assert exchangeRate != null;
            BigDecimal exchangeRateValue = BigDecimal.valueOf(exchangeRate.getSell());

            List<Item> items = new ArrayList<>();
            BigDecimal subtotalUSD = BigDecimal.ZERO;

            for (TblPublication pub : transaction.getPublications()) {
                BigDecimal priceInColones = BigDecimal.valueOf(pub.getPrice());
                BigDecimal priceInUSD = priceInColones.divide(exchangeRateValue, 2, RoundingMode.HALF_UP);
                subtotalUSD = subtotalUSD.add(priceInUSD);

                Item item = new Item();
                item.setName(pub.getTitle())
                        .setCurrency("USD")
                        .setPrice(priceInUSD.toString())
                        .setQuantity("1");
                items.add(item);
            }

            BigDecimal taxUSD = subtotalUSD
                    .multiply(new BigDecimal("0.13"))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalUSD = subtotalUSD.add(taxUSD);

            Details details = new Details();
            details.setSubtotal(subtotalUSD.toString());
            details.setTax(taxUSD.toString());

            Amount amount = new Amount();
            amount.setCurrency("USD");
            amount.setTotal(totalUSD.toString());
            amount.setDetails(details);

            Transaction paypalTransaction = new Transaction();
            String description = transaction.getPublications().stream()
                    .map(TblPublication::getTitle)
                    .findFirst()
                    .orElse("Compra de animales");
            paypalTransaction.setDescription(description);
            paypalTransaction.setAmount(amount);
            paypalTransaction.setItemList(new ItemList().setItems(items));
            paypalTransaction.setCustom(transaction.getId().toString());

            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(new Payer().setPaymentMethod("paypal"));
            payment.setTransactions(Collections.singletonList(paypalTransaction));

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl("http://localhost:4200/app/paypal/cancel?transactionId=" + transaction.getId());
            redirectUrls.setReturnUrl("http://localhost:4200/app/paypal/success?transactionId=" + transaction.getId());
            payment.setRedirectUrls(redirectUrls);

            return payment.create(apiContext);
        } catch (Exception e) {
            log.error("Error al crear el pago en PayPal: {}", e.getMessage(), e);
            throw new PayPalRESTException(e.getMessage());
        }
    }

    /**
     * ejecuta un pago previamente aprobado por el usuario en paypal
     *
     * @param paymentId el id del pago en paypal
     * @param payerId   el id del pagador en paypa
     * @return el objeto payment ejecutado en paypal
     * @throws PayPalRESTException si ocurre un error al ejecutar el pago en paypal
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }

    /**
     * obtiene la url de aprobación para que el usuario autorice el pago en paypal
     *
     * @param payment el objeto payment del cual se desea obtener la url de aprobación
     * @return la url de aprobación si está disponible, de lo contrario null
     */
    public String getApprovalUrl(Payment payment) {
        return payment.getLinks().stream()
                .filter(link -> "approval_url".equalsIgnoreCase(link.getRel()))
                .map(Links::getHref)
                .findFirst()
                .orElse(null);
    }
}

