package com.project.demo.logic.entity.publication;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.demo.logic.entity.bid.TblBid;
import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.photo.TblPhoto;
import com.project.demo.logic.entity.transaction.TblTransaction;
import com.project.demo.logic.entity.user.TblUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TBL_Publication")
public class TblPublication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Publication_Id", nullable = false)
    private Long id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Specie", nullable = false)
    private String specie;

    @Column(name = "Race", nullable = false)
    private String race;

    @Column(name = "Gender", nullable = false)
    private String gender;

    @Column(name = "Weight", nullable = false)
    private Long weight;

    @Column(name = "Birth_Date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "SENASA_Certificate", nullable = false)
    private String senasaCertificate;

    @Column(name = "Price", nullable = true)
    private Long price;

    @Column(name = "Start_Date")
    private LocalDateTime startDate;

    @Column(name = "End_Date")
    private LocalDateTime endDate;

    @Column(name = "Minimum_Increase", nullable = true)
    private Integer minimumIncrease;

    @Column(name = "Type", nullable = false)
    private String type;

    @Column(name = "State", nullable = false)
    private String state;

    @Column(name = "Creation_Date", nullable = false)
    private Instant creationDate;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "Direction_Id", nullable = false)
    private TblDirection direction;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "User_Id", nullable = false)
    private TblUser user;

    @JsonManagedReference
    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TblPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TblBid> bids = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Transaction_Id")
    @JsonBackReference
    private TblTransaction transaction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecie() {
        return specie;
    }

    public void setSpecie(String specie) {
        this.specie = specie;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getSenasaCertificate() {
        return senasaCertificate;
    }

    public void setSenasaCertificate(String senasaCertificate) {
        this.senasaCertificate = senasaCertificate;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getMinimumIncrease() {
        return minimumIncrease;
    }

    public void setMinimumIncrease(Integer minimumIncrease) {
        this.minimumIncrease = minimumIncrease;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public TblDirection getDirection() {
        return direction;
    }

    public void setDirection(TblDirection direction) {
        this.direction = direction;
    }

    public TblUser getUser() {
        return user;
    }

    public void setUser(TblUser user) {
        this.user = user;
    }

    public List<TblPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<TblPhoto> photos) {
        this.photos = photos;
    }

    public List<TblBid> getBids() {
        return bids;
    }

    public void setBids(List<TblBid> bids) {
        this.bids = bids;
    }

    public TblTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(TblTransaction transaction) {
        this.transaction = transaction;
    }
}