package com.project.demo.rest.bill;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.project.demo.logic.entity.bill.BillService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.transaction.TblTransaction;
import com.project.demo.logic.entity.transaction.TblTransactionRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.utils.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

@RestController
@RequestMapping("/bills")
public class BillRestController {
    @Autowired
    private TblTransactionRepository transactionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BillService billService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblTransactionRepository tblTransactionRepository;

    @GetMapping
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TblTransaction> transactionsPage = transactionRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(transactionsPage.getTotalPages());
        meta.setTotalElements(transactionsPage.getTotalElements());
        meta.setPageNumber(transactionsPage.getNumber() + 1);
        meta.setPageSize(transactionsPage.getSize());

        return new GlobalResponseHandler().handleResponse("Transacciones recuperadas correctamente.",
                transactionsPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'BUYER')")
    public ResponseEntity<?> getTransactionByUserId(@PathVariable long userId, @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    HttpServletRequest request) {
        Optional<TblUser> foundUser = userRepository.findById(userId);
        if (foundUser.isPresent()) {
            Pageable pageable = PageRequest.of(page-1, size);
            Page<TblTransaction> publicationsPage = tblTransactionRepository.findTblTransactionsByUserId(userId, pageable);
            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
            meta.setTotalPages(publicationsPage.getTotalPages());
            meta.setTotalElements(publicationsPage.getTotalElements());
            meta.setPageNumber(publicationsPage.getNumber() + 1);
            meta.setPageSize(publicationsPage.getSize());

            return new GlobalResponseHandler().handleResponse("Transacciones recuperadas correctamente",
                    publicationsPage.getContent(), HttpStatus.OK, meta);
        }else {
            return new GlobalResponseHandler().handleResponse("Usuario " + userId + " no encontrado",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'BUYER')")
    public ResponseEntity<?> getTransactionById(@PathVariable Long transactionId, HttpServletRequest request) {
        Optional<TblTransaction> foundTransaction = transactionRepository.findById(transactionId);
        if (foundTransaction.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Transacción recuperada correctamente.",
                    foundTransaction.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("ID de Transacción" + transactionId + " no encontrada.",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long transactionId, HttpServletRequest request) {
        Optional<TblTransaction> foundTransaction = transactionRepository.findById(transactionId);
        if (foundTransaction.isPresent()) {
            transactionRepository.deleteById(transactionId);
            return new GlobalResponseHandler().handleResponse("Transacción eliminada correctamente",
                    foundTransaction.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("ID de Transacción" + transactionId + " no encontrada.",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/emailBill")
    public ResponseEntity<?> sendBill(@RequestBody TblTransaction transaction, HttpServletRequest request) {
        try {
            emailService.sendBillEmail(transaction.getUser().getId(), billService.generateBillBody(transaction));
            return new GlobalResponseHandler().handleResponse("Factura enviada",
                    HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Error al enviar las factura " + e.getMessage(),
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPDF(@RequestBody TblTransaction transaction) {
        try {
            String formattedHtmlContent = billService.generateHtmlContent(billService.generateBillBody(transaction));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            ConverterProperties converterProperties = new ConverterProperties();
            HtmlConverter.convertToPdf(formattedHtmlContent, pdfDocument, converterProperties);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura.pdf");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}