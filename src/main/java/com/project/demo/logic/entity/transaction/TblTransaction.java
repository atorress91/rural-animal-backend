package com.project.demo.logic.entity.transaction;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.user.TblUser;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TBL_Transaction")
public class TblTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Transaction_Id", nullable = false)
    private Long id;

    @Column(name = "Status", nullable = false)
    private String status;

    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "tax", nullable = false)
    private BigDecimal tax;

    @Column(name = "Creation Date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "User_Id", nullable = false)
    private TblUser user;

    @JsonManagedReference
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
    private List<TblPublication> publications = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TblUser getUser() {
        return user;
    }

    public void setUser(TblUser user) {
        this.user = user;
    }

    public List<TblPublication> getPublications() {
        return publications;
    }

    public void setPublications(List<TblPublication> publications) {
        this.publications = publications;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public LocalDateTime getCreationDate() { return creationDate; }

    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
}