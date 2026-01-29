package com.project.demo.logic.entity.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService<T> {
    private final RedisTemplate<String, T> redisTemplate;

    public RedisCacheService(RedisTemplate<String, T> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Obtiene un valor del caché.
     *
     * @param key La clave para buscar en el caché
     * @return Optional con el valor si existe, empty si no
     */
    public Optional<T> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    /**
     * Guarda un valor en el caché con un tiempo de expiración.
     *
     * @param key      La clave para almacenar el valor
     * @param value    El valor a almacenar
     * @param duration Duración antes de que expire el valor
     */
    public void set(String key, T value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /**
     * Guarda un valor en el caché con un tiempo de expiración específico.
     *
     * @param key     La clave para almacenar el valor
     * @param value   El valor a almacenar
     * @param timeout Tiempo antes de que expire el valor
     * @param unit    Unidad de tiempo para el timeout
     */
    public void set(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Elimina un valor del caché.
     *
     * @param key La clave del valor a eliminar
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Verifica si una clave existe en el caché.
     *
     * @param key La clave a verificar
     * @return true si la clave existe, false si no
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Obtiene o calcula un valor si no existe en el caché.
     *
     * @param key      La clave para buscar/almacenar el valor
     * @param duration Duración antes de que expire el valor
     * @param supplier Función que calcula el valor si no está en caché
     * @return El valor del caché o el calculado
     */
    public T getOrSet(String key, Duration duration, CacheSupplier<T> supplier) {
        Optional<T> cachedValue = get(key);
        if (cachedValue.isPresent()) {
            return cachedValue.get();
        }

        T value = supplier.get();
        set(key, value, duration);
        return value;
    }
}