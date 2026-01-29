package com.project.demo.logic.entity.photo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TblPhotoRepository extends JpaRepository<TblPhoto, Long> {

}
