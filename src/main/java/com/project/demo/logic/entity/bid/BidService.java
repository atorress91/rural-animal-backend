package com.project.demo.logic.entity.bid;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * servicio que gestiona las operaciones relacionadas con publicaciones
 */
@Service
public class BidService {

    private final TblPublicationRepository publicationRepository;
    private final TblBidRepository tblBidRepository;
    private final UserRepository tblUserRepository;

    /**
     * Construye un nuevo servicio de pujas con los repositorios especificados.
     *
     * @param tblBidRepository         el repositorio de pujas
     * @param publicationRepository el repositorio de publicaciones
     * @param tblUserRepository        el repositorio de usuarios
     */
    public BidService(TblBidRepository tblBidRepository, TblPublicationRepository publicationRepository, UserRepository tblUserRepository) {
        this.tblBidRepository = tblBidRepository;
        this.publicationRepository = publicationRepository;
        this.tblUserRepository = tblUserRepository;
    }

    /**
     * obtiene la puja más alta
     *
     * @param bids lista pujas a comparar
     * @return puja más alta
     */
    public TblBid findHighestBid(List<TblBid> bids) {
        if (bids == null || bids.isEmpty()) {
            throw new IllegalArgumentException("La lista de pujas está vacía o es nula.");
        }

        return bids.stream()
                .max(Comparator.comparingLong(TblBid::getBidAmmount))
                .orElseThrow(() -> new IllegalStateException("No se pudo encontrar la puja más alta."));
    }

    /**
     * Realiza una puja en una publicación especificada por un usuario especificado.
     *
     * @param publicationId el ID de la publicación
     * @param userId        el ID del usuario
     * @return un mapa que contiene los detalles de la puja
     * @throws Exception si no se encuentra la publicación o el usuario, o si hay un problema con la puja
     */
    @Transactional
    public Map<String, Object> placeBid(Long publicationId, Long userId) throws Exception {
        TblPublication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new Exception("Publicación no encontrada"));

        TblUser user = tblUserRepository.findById(userId)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));


        if (!"Subasta".equals(publication.getType())) {
            throw new Exception("Esta publicación no es una subasta");
        }

        LocalDateTime now = LocalDateTime.now();
        if (publication.getStartDate().isAfter(now)) {
            throw new Exception("La subasta aún no ha comenzado");
        }

        if (publication.getEndDate().isBefore(now)) {
            throw new Exception("La subasta ya ha finalizado");
        }

        if (!"Activa".equals(publication.getState())) {
            throw new Exception("La subasta no está activa");
        }

        if (publication.getUser().getId().equals(user.getId())) {
            throw new Exception("No puedes pujar en tu propia subasta");
        }

        Long highestBid = publication.getBids().stream()
                .filter(bid -> "ACTIVE".equals(bid.getStatus()))
                .map(TblBid::getBidAmmount)
                .max(Long::compareTo)
                .orElse(publication.getPrice());

        if (publication.getMinimumIncrease() == null) {
            throw new Exception("No se ha establecido el incremento mínimo para esta subasta");
        }

        Long newBidAmount = highestBid + publication.getMinimumIncrease();

        TblBid bid = new TblBid();
        bid.setBidAmmount(newBidAmount);
        bid.setUser(user);
        bid.setPublication(publication);
        bid.setStatus("ACTIVE");
        bid.setBidDate(now);

        tblBidRepository.save(bid);

        Map<String, Object> bidInfo = new HashMap<>();
        bidInfo.put("bid", bid);
        bidInfo.put("bidderName", user.getName());
        bidInfo.put("bidderId", user.getId());
        bidInfo.put("bidAmount", bid.getBidAmmount());
        bidInfo.put("bidDate", bid.getBidDate());

        return bidInfo;
    }

    /**
     * Recupera la última puja para una publicación especificada.
     *
     * @param publicationId el ID de la publicación
     * @return un optional que contiene la última puja, o vacío si no se encuentra ninguna puja
     */
    public Optional<TblBid> getLatestBidByPublication(Long publicationId) {
        return tblBidRepository.findLatestBidByAuctionId(publicationId);
    }
}