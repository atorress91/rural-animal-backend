package com.project.demo.logic.entity.bid;

import com.project.demo.logic.entity.user.TblUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TblBidRepository extends JpaRepository<TblBid, Long> {

    // Obtener las pujas de una publicacion
    @Query("SELECT p FROM TblBid p WHERE p.publication.id = :id")
    List<TblBid> findBidsByPublicationId(@Param("id") Long id);


    List<TblBid> findByUserAndStatus(TblUser user, String status);

    /**
     * Encuentra la última puja activa para una subasta dada.
     *
     * @param auctionId el ID de la subasta
     * @return un optional que contiene la última puja activa, o vacío si no se encuentra ninguna
     */
    @Query("SELECT b FROM TblBid b WHERE b.publication.id = :auctionId AND b.status = 'ACTIVE' ORDER BY b.id DESC LIMIT 1")
    Optional<TblBid> findLatestBidByAuctionId(@Param("auctionId") Long auctionId);
}