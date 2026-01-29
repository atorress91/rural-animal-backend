package com.project.demo.rest.paypal;

import com.paypal.api.payments.Payment;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.paypal.PayPalResponse;
import com.project.demo.logic.entity.paypal.PayPalService;
import com.project.demo.logic.entity.paypal.PayPalRequest;
import com.project.demo.logic.entity.transaction.TblTransaction;
import com.project.demo.logic.entity.transaction.TransactionService;
import com.project.demo.logic.entity.user.TblUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

/**
 * controlador REST para manejar pagos a través de paypal
 */
@RequestMapping("/paypal")
@RestController
public class PayPalRestController {

    private final PayPalService payPalService;
    private final TransactionService transactionService;

    /**
     * constructor de paypal rest controller
     *
     * @param payPalService      servicio para manejar pagos de paypal
     * @param transactionService servicio para crear o actualizar la transacciones
     */
    public PayPalRestController(PayPalService payPalService, TransactionService transactionService) {
        this.payPalService = payPalService;
        this.transactionService = transactionService;
    }

    /**
     * crea un nuevo pago a través de paypal
     *
     * @param paymentRequest objeto que contiene la información del pago
     * @param request        objeto http servlet request para obtener la información de la solicitud
     * @return ResponseEntity con la url de aprobación del pago o un mensaje de error
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPayment(@RequestBody PayPalRequest paymentRequest, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TblUser user = (TblUser) authentication.getPrincipal();

        try {

            TblTransaction transaction = transactionService.createPendingTransaction(user, paymentRequest);

            Payment payment = payPalService.createPayment(transaction);

            String approvalUrl = payPalService.getApprovalUrl(payment);
            if (approvalUrl != null) {
                return new GlobalResponseHandler().handleResponse(
                        "Payment created successfully",
                        approvalUrl,
                        HttpStatus.OK,
                        request
                );
            } else {
                return new GlobalResponseHandler().handleResponse(
                        "No approval URL found",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        request
                );
            }
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse(
                    "Error creating payment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    request
            );
        }
    }

    /**
     * ejecuta un pago previamente aprobado por paypal
     *
     * @param paymentId del pago de paypal
     * @param payerId   del pagador de paypal
     * @param request   objeto http servlet request para obtener la información de la solicitud
     * @return response entity con la respuesta de la ejecución del pago o un mensaje de error
     */
    @GetMapping("/success")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> executePayment(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            HttpServletRequest request) {
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            String transactionIdStr = payment.getTransactions().getFirst().getCustom();
            Long transactionId = Long.parseLong(transactionIdStr);
            if ("approved".equalsIgnoreCase(payment.getState())) {
                transactionService.updateTransactionStatus(transactionId, "APPROVED");
                PayPalResponse paymentResponse = new PayPalResponse(
                        payment.getId(),
                        payment.getState(),
                        payment.getTransactions().getFirst().getAmount().getTotal(),
                        payment.getTransactions().getFirst().getAmount().getCurrency()
                );
                return new GlobalResponseHandler().handleResponse(
                        "Payment executed successfully",
                        paymentResponse,
                        HttpStatus.OK,
                        request
                );
            } else {
                transactionService.updateTransactionStatus(transactionId, "FAILED");
                return new GlobalResponseHandler().handleResponse(
                        "Payment not approved",
                        HttpStatus.BAD_REQUEST,
                        request
                );
            }
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse(
                    "Error executing payment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    request
            );
        }
    }

    /**
     * cancela un pago en proceso
     *
     * @param transactionId de la transacción a cancelar
     * @param request       objeto http servlet request para obtener la información de la solicitud
     * @return response entity  confirmando la cancelación del pago
     */
    @GetMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelPayment(@RequestParam("transactionId") Long transactionId, HttpServletRequest request) {
        transactionService.updateTransactionStatus(transactionId, "CANCELLED");
        return new GlobalResponseHandler().handleResponse(
                "Payment cancelled by user",
                HttpStatus.OK,
                request
        );
    }
}