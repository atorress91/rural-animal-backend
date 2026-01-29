package com.project.demo.logic.entity.role;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Role")
public class TblRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Role_Id", nullable = false)
    private Long id;

    @Column(name = "Title", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum title;


    @Column(name = "Description", nullable = false)
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleEnum getTitle() {
        return title;
    }

    public void setTitle(RoleEnum title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}