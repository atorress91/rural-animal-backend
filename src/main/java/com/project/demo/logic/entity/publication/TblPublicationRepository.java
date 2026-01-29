package com.project.demo.logic.entity.publication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

public interface TblPublicationRepository extends JpaRepository<TblPublication, Long> {

    //  "Venta", sin publicaciones eliminadas
    @Query("SELECT p FROM TblPublication p WHERE p.type = 'Venta' AND p.state = 'Activa'")
    Page<TblPublication> findAllSales(Pageable pageable);

    //  "Subasta", sin publicaciones eliminadas
    @Query("SELECT p FROM TblPublication p WHERE p.type = 'Subasta' AND p.state = 'Activa'")
    Page<TblPublication> findAllAuctions(Pageable pageable);

    // Obtener las publicaciones activas de un usuario
    @Query("SELECT p FROM TblPublication p WHERE " +
            "(p.state != 'Deleted') " + // Publicaciones que no estén eliminadas
            "AND (p.user.id = :id)" +
            "AND (:type IS NULL OR :type = '' OR p.type = :type) " +  // Filtro por tipo
            "AND (:search IS NULL OR :search = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.specie) LIKE LOWER(CONCAT('%', :search, '%'))) " +  // Filtro por búsqueda
            "ORDER BY " +
            "CASE WHEN :sort = 'precio' THEN p.price END ASC, " +  // Ordenar por precio (orden ascendente por defecto)
            "CASE WHEN :sort = 'especie' THEN p.specie END ASC") // Ordenar por especie (orden ascendente por defecto))
    Page<TblPublication> findTblPublicationsByUserId(
            @Param("id") Long id,
            @Param("type") String type,
            @Param("search") String search,
            @Param("sort") String sort,
            Pageable pageable);

    // Obtener todas las publicaciones activas
    @Query("SELECT p FROM TblPublication p WHERE p.state != 'Deleted'")
    Page<TblPublication> findAll(Pageable pageable);

    @Query("SELECT p FROM TblPublication p WHERE " +
            "(p.state != 'Deleted') " +  // Publicaciones que no estén eliminadas
            "AND (:type IS NULL OR :type = '' OR p.type = :type) " +  // Filtro por tipo
            "AND (:search IS NULL OR :search = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.specie) LIKE LOWER(CONCAT('%', :search, '%'))) " +  // Filtro por búsqueda
            "ORDER BY " +
            "CASE WHEN :sort = 'precio' THEN p.price END ASC, " +  // Ordenar por precio (orden ascendente por defecto)
            "CASE WHEN :sort = 'especie' THEN p.specie END ASC") // Ordenar por especie (orden ascendente por defecto)
    Page<TblPublication> findFilteredPublications(
            @Param("type") String type,
            @Param("search") String search,
            @Param("sort") String sort,
            Pageable pageable);

    /**
     * Busca subastas activas en una fecha específica.
     *
     * @param date La fecha en la que se desea buscar subastas.
     *             La consulta filtra subastas donde la fecha de inicio es igual a este valor.
     * @param pageable Información de paginación para controlar el número de resultados
     *                 devueltos y la página actual.
     * @return Una página de publicaciones que corresponde a subastas activas en la fecha especificada.
     */
    @Query("SELECT p FROM TblPublication p WHERE p.type = 'Subasta' " +
           "AND CAST(p.startDate AS date) = :date " +
           "AND p.state = 'Activa'")
    Page<TblPublication> findActiveAuctionsByDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT p FROM TblPublication p WHERE" +
           "(p.state != 'Deleted' AND p.state != 'Vendido') AND " +
           "(:species IS NULL OR LOWER(p.specie) ILIKE CONCAT('%', LOWER(:species), '%')) AND " +
           "(:breed IS NULL OR LOWER(p.race) ILIKE CONCAT('%', LOWER(:breed), '%'))")
    Page<TblPublication> findBySpeciesAndBreed(
            @Param("species") String species,
            @Param("breed") String breed,
            Pageable pageable);

    @Query("SELECT p FROM TblPublication p WHERE p.type = ?1 AND p.state = ?2 AND p.endDate < ?3")
    List<TblPublication> findAuctionsToComplete(String type, String state, LocalDateTime endDate);
}