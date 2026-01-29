package com.project.demo.logic.entity.t3dModel;

import com.project.demo.logic.entity.animal.TblAnimal;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_3D_Model")
public class Tbl3dModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "C_3D_Model_Id", nullable = false)
    private Long id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Url", nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "Animal_Id", nullable = false)
    private TblAnimal animal;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TblAnimal getAnimal() {
        return animal;
    }

    public void setAnimal(TblAnimal animal) {
        this.animal = animal;
    }

}