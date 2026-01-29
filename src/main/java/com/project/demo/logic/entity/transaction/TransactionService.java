package com.project.demo.logic.entity.transaction;

import com.project.demo.logic.entity.bill.BillService;
import com.project.demo.logic.entity.paypal.PayPalRequest;
import com.project.demo.logic.entity.publication.PublicationService;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.utils.EmailService;
import com.project.demo.rest.bill.BillRestController;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * servicio para manejar las transacciones relacionadas con las publicaciones y los usuarios
 * proporciona funcionalidades para crear transacciones pendientes y actualizar su estado
 */
@Service
@Slf4j
public class TransactionService {
    private final TblTransactionRepository transactionRepository;
    private final PublicationService publicationService;
    private final EmailService emailService;
    private final BillService billService;

    public TransactionService(TblTransactionRepository transactionRepository, PublicationService publicationService) {
        this.transactionRepository = transactionRepository;
        this.publicationService = publicationService;
        this.emailService = new EmailService();
        this.billService = new BillService();
    }

    /**
     * crea una nueva transacción en estado "PENDING" (pendiente) y la asocia con las publicaciones y el usuario
     * también calcula los valores de subtotal, impuesto y total de la transacción
     *
     * @param user           usuario asociado a la transacción
     * @param paymentRequest detalles de la solicitud de pago (por ejemplo, publicaciones seleccionadas)
     * @return la transacción creada y guardada en la base de datos
     */
    @Transactional
    public TblTransaction createPendingTransaction(TblUser user, PayPalRequest paymentRequest) {
        try {
            TblTransaction transaction = new TblTransaction();
            transaction.setStatus("PENDING");
            transaction.setUser(user);

            List<TblPublication> publications = publicationService.getPublicationsByIds(
                    paymentRequest.getPublications()
            );

            for (TblPublication publication : publications) {
                publication.setTransaction(transaction);
                transaction.getPublications().add(publication);
            }

            BigDecimal subtotalColones = publications.stream()
                    .map(pub -> BigDecimal.valueOf(pub.getPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal taxColones = subtotalColones
                    .multiply(new BigDecimal("0.13"))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalColones = subtotalColones.add(taxColones);

            transaction.setSubTotal(subtotalColones);
            transaction.setTax(taxColones);
            transaction.setTotal(totalColones);

            log.debug("Guardando transacción con {} publicaciones", publications.size());
            TblTransaction savedTransaction = transactionRepository.saveAndFlush(transaction);
            log.debug("Transacción guardada con id: {}", savedTransaction.getId());

            return savedTransaction;
        } catch (Exception e) {
            log.error("Error creando transacción pendiente: ", e);
            throw e;
        }
    }

    /**
     * actualiza el estado de una transacción existente en la base de datos
     * si el estado se actualiza a "APPROVED", también se actualiza el estado
     * de las publicaciones asociadas a "Vendido"
     *
     * @param transactionId ID de la transacción a actualizar
     * @param status        nuevo estado de la transacción (por ejemplo, "PENDING", "APPROVED")
     */
    @Transactional
    public void updateTransactionStatus(Long transactionId, String status) {
        TblTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        transaction.setStatus(status);

        if ("APPROVED".equals(status)) {
            List<Long> publicationIds = transaction.getPublications().stream()
                    .map(TblPublication::getId)
                    .collect(Collectors.toList());

            publicationService.updatePublicationsState(publicationIds, "Vendido");
         //   emailService.sendBillEmail(transaction.getUser().getId(), billService.generateBillBody(transaction));
        }

        transactionRepository.save(transaction);
    }
}