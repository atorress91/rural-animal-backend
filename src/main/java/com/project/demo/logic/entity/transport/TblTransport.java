package com.project.demo.logic.entity.transport;

import com.project.demo.logic.entity.transaction.TblTransaction;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Transport")
public class TblTransport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Transport_Id", nullable = false)
    private Long id;

    @Column(name = "State", nullable = false)
    private String state;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "Transaction_Id", nullable = false)
    private TblTransaction transaction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public TblTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(TblTransaction transaction) {
        this.transaction = transaction;
    }

}