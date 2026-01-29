package com.project.demo.logic.entity.bid;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.user.TblUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_Bid")
public class TblBid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Bid_Id", nullable = false)
    private Long id;

    @Column(name = "Bid_Ammount", nullable = false)
    private Long bidAmmount;

    @Column(name = "Bid_Date", nullable = false)
    private LocalDateTime bidDate;

    @Column(name = "Status", nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "User_Id", nullable = false)
    private TblUser user;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "Publication_Id", nullable = false)
    private TblPublication publication;

    public TblBid() {
        this.bidDate = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBidAmmount() {
        return bidAmmount;
    }

    public void setBidAmmount(Long bidAmmount) {
        this.bidAmmount = bidAmmount;
    }

    public TblUser getUser() {
        return user;
    }

    public void setUser(TblUser user) {
        this.user = user;
    }

    public TblPublication getPublication() {
        return publication;
    }

    public void setPublication(TblPublication publication) {
        this.publication = publication;
    }

    public LocalDateTime getBidDate() {
        return bidDate;
    }

    public void setBidDate(LocalDateTime bidDate) {
        this.bidDate = bidDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}