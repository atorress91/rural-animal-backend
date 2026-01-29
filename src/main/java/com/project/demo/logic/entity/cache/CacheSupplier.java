package com.project.demo.logic.entity.cache;

/**
 * Interfaz funcional para proveer valores cuando no están en caché
 */
@FunctionalInterface
public interface CacheSupplier<T> {
    T get();
}