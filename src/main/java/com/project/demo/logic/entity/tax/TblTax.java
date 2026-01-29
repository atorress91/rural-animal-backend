package com.project.demo.logic.entity.tax;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Tax")
public class TblTax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Tax_Id", nullable = false)
    private Long id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Description", nullable = false)
    private String description;

    @Column(name = "Tax_Value", nullable = false)
    private Long taxValue;

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

    public Long getTaxValue() {
        return taxValue;
    }

    public void setTaxValue(Long taxValue) {
        this.taxValue = taxValue;
    }

}