package com.project.demo.logic.entity.publication;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * servicio que gestiona las operaciones relacionadas con publicaciones
 */
@Service
public class PublicationService {
    private final TblPublicationRepository publicationRepository;

    /**
     * constructor de publication service
     *
     * @param publicationRepository repositorio para gestionar las publicaciones en la base de datos
     */
    public PublicationService(TblPublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    /**
     * obtiene una lista de publicaciones por sus ids
     *
     * @param publicationIds lista de ids en las publicaciones a buscar
     * @return lista de publicaciones
     */
    public List<TblPublication> getPublicationsByIds(List<BigInteger> publicationIds) {
        if (publicationIds == null || publicationIds.isEmpty()) {
            throw new IllegalArgumentException("Publication IDs list cannot be null or empty");
        }

        List<Long> ids = publicationIds.stream()
                .map(BigInteger::longValue)
                .collect(Collectors.toList());

        List<TblPublication> publications = publicationRepository.findAllById(ids);

        if (publications.size() != publicationIds.size()) {
            throw new EntityNotFoundException("Some publications were not found");
        }

        return publications;
    }

    /**
     * actualiza el estado de múltiples publicaciones
     *
     * @param publicationIds lista de ids de las publicaciones a actualizar
     * @param newState       nuevo estado para las publicaciones
     */
    @Transactional
    public void updatePublicationsState(List<Long> publicationIds, String newState) {
        List<TblPublication> publications = publicationRepository.findAllById(publicationIds);

        if (publications.size() != publicationIds.size()) {
            throw new EntityNotFoundException("Some publications were not found");
        }

        publications.forEach(publication -> publication.setState(newState));
        publicationRepository.saveAll(publications);
    }
}