package com.project.demo.logic.entity.veterinary;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Veterinary")
public class TblVeterinary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Veterinary_Id", nullable = false)
    private Long id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Last_Name1", nullable = false)
    private String lastName1;

    @Column(name = "Last_Name2")
    private String lastName2;

    @Column(name = "Identification", nullable = false)
    private String identification;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "Phone_Number", nullable = false)
    private String phoneNumber;

    @Column(name = "Institution", nullable = false)
    private String institution;

    @Column(name = "Speciallity", nullable = false)
    private String speciallity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName1() {
        return lastName1;
    }

    public void setLastName1(String lastName1) {
        this.lastName1 = lastName1;
    }

    public String getLastName2() {
        return lastName2;
    }

    public void setLastName2(String lastName2) {
        this.lastName2 = lastName2;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getSpeciallity() {
        return speciallity;
    }

    public void setSpeciallity(String speciallity) {
        this.speciallity = speciallity;
    }

}