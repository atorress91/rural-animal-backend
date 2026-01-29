package com.project.demo.rest.notificate;

import com.project.demo.logic.entity.bid.BidService;
import com.project.demo.logic.entity.bid.TblBid;
import com.project.demo.logic.entity.bid.TblBidRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.notification.TblNotification;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.veterinaryAppointment.TblVeterinaryAppointment;
import com.project.demo.logic.utils.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestionar las notificaciones por correo electrónico.
 * Este controlador maneja el envío de notificaciones relacionadas con subastas
 * y potencialmente con citas veterinarias.
 */
@RestController
@RequestMapping("/notifications")
public class NotificateEmailRestController {
    /**
     * Servicio para enviar correos electrónicos a los usuarios.
     */
    @Autowired
    private EmailService emailService;

    /**
     * Servicio para gestionar las operaciones relacionadas con las ofertas en subastas.
     */
    @Autowired
    private BidService bidService;

    /**
     * Repositorio para acceder a los datos de los usuarios.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Repositorio para acceder a los datos de las publicaciones.
     */
    @Autowired
    private TblPublicationRepository publicationRepository;

    /**
     * Repositorio para acceder a los datos de las ofertas en subastas.
     */
    @Autowired
    private TblBidRepository bidRepository;

    /**
     * Notifica a los participantes de una subasta sobre los resultados.
     * 
     * Este método envía correos electrónicos a:
     * - Todos los participantes que no ganaron la subasta
     * - El ganador de la subasta con un mensaje especial
     * - El propietario de la publicación
     *
     * @param publicationId El ID de la publicación de la subasta
     * @param request La solicitud HTTP
     * @return ResponseEntity con el resultado de la operación
     */
    @PostMapping("/auction")
    public ResponseEntity<?> notificateWinner(@RequestBody Long publicationId, HttpServletRequest request) {
        try {
            Optional<TblPublication> foundPublication = publicationRepository.findById(publicationId);

            if (foundPublication.isPresent()) {
                List<TblBid> bids = bidRepository.findBidsByPublicationId(foundPublication.get().getId());

                if (bids == null || bids.isEmpty()) {
                    return ResponseEntity.badRequest().body("No hay ofertas para esta publicación.");
                }

                TblBid highestBid = bidService.findHighestBid(bids);

                for (TblBid bid : bids) {
                    if (!bid.getUser().getId().equals(highestBid.getUser().getId())) {
                        emailService.notificateAuctionBidder(bid, foundPublication.get());
                    }
                }
                emailService.notificateAuctionWinner(highestBid, foundPublication.get());
                emailService.notificatePublicationOwner(foundPublication.get());
            }

            return new GlobalResponseHandler().handleResponse("Notificaciónes enviadas",
                    foundPublication, HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Error al enviar las notificaciones"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Notifica sobre una cita veterinaria.
     * 
     * Este método está actualmente comentado y no implementado completamente.
     * Cuando se active, enviará notificaciones por correo electrónico relacionadas
     * con citas veterinarias.
     *
     * @param appointment La cita veterinaria sobre la que se enviará la notificación
     * @param request La solicitud HTTP
     * @return ResponseEntity con el resultado de la operación
     */
    /*
    @PostMapping("/appointment")
    public ResponseEntity<?> notificateAppointment(@RequestBody TblVeterinaryAppointment appointment, HttpServletRequest request) {
        try {
            emailService.notificateAppointment();

            return new GlobalResponseHandler().handleResponse("Notificación enviada",
                    new TblNotification(), HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Error al enviar la notificación"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }
    */
}
