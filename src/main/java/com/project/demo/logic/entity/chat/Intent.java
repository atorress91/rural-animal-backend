package com.project.demo.logic.entity.chat;

/**
 * Enum que representa las diferentes intenciones (intents) en el sistema de chat.
 */
public enum Intent {

    /** Intención para gestionar citas veterinarias. */
    VET_APPOINTMENT,

    /** Intención para realizar consultas sobre subastas. */
    AUCTION_QUERY,

    /** Intención para obtener información de un animal. */
    ANIMAL_INFO,

    /**
     * Intención desconocida o no reconocida en el sistema. Se utiliza cuando no se puede
     * identificar la intención del mensaje proporcionado por el usuario. Puede actuar como
     * un mecanismo de seguridad para una consulta general.
     */
    UNKNOWN
}
