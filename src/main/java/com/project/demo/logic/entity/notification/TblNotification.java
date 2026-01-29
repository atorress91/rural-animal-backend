package com.project.demo.logic.entity.notification;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.user.TblUser;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_Notification")
public class TblNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Notification_Id", nullable = false)
    private Long id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Description", nullable = false)
    private String description;

    @Column(name = "Type", nullable = false)
    private String type;

    @Column(name = "Creation Date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Publication_Id")
    private TblPublication publication;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "User_Id", nullable = false)
    private TblUser user;

    @Column(name = "State", nullable = false)
    private String state;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreationDate() { return creationDate; }

    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public TblPublication getPublication() {
        return publication;
    }

    public void setPublication(TblPublication publication) {
        this.publication = publication;
    }

    public TblUser getUser() {
        return user;
    }

    public void setUser(TblUser user) {
        this.user = user;
    }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }
}