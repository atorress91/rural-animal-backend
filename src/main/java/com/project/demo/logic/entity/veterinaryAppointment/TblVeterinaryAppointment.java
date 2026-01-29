package com.project.demo.logic.entity.veterinaryAppointment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.demo.logic.entity.notification.TblNotification;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.veterinary.TblVeterinary;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_Veterinary_Appointment")
public class TblVeterinaryAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Veterinary_Appointment_Id", nullable = false)
    private Long id;

    @Column(name = "Start_Date", nullable = false)
    @JsonFormat(timezone = "America/Costa_Rica")
    private LocalDateTime startDate;

    @Column(name="End_Date",nullable = false)
    @JsonFormat(timezone = "America/Costa_Rica")
    private LocalDateTime endDate;

    @Column(name = "State", nullable = false)
    private String state;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Notification_Id")
    private TblNotification notification;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "Veterinary_Id", nullable = false)
    private TblVeterinary veterinary;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "User_Id", nullable = false)
    private TblUser user;

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

    public TblNotification getNotification() {
        return notification;
    }

    public void setNotification(TblNotification notification) {
        this.notification = notification;
    }

    public TblVeterinary getVeterinary() {
        return veterinary;
    }

    public void setVeterinary(TblVeterinary veterinary) {
        this.veterinary = veterinary;
    }

    public TblUser getUser() {
        return user;
    }

    public void setUser(TblUser user) {
        this.user = user;
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
}