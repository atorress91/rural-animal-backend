package com.project.demo.logic.entity.photo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.demo.logic.entity.publication.TblPublication;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Photo")
public class TblPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Photo_Id", nullable = false)
    private Long id;

    @Column(name = "Url", nullable = false)
    private String url;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column (name = "Cloudinary_Id", nullable = false)
    private String cloudinaryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Publication_Id")
    @JsonBackReference
    private TblPublication publication;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TblPublication getPublication() {
        return publication;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCloudinaryId() {
        return cloudinaryId;
    }

    public void setCloudinaryId(String cloudinaryId) {
        this.cloudinaryId = cloudinaryId;
    }

    public void setPublication(TblPublication publication) {
        this.publication = publication;
    }
}