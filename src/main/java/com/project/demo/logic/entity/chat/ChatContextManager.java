package com.project.demo.logic.entity.chat;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChatContextManager {
    private final Map<Long, ConversationContext> userContexts = new HashMap<>();

    @Data
    public static class ConversationContext {
        private String selectedDate;
        private String selectedVetName;
        private String lastShownSchedule;
        private boolean isCheckingAvailability;
        private LocalDateTime lastInteraction;
        private Intent currentIntent;
        private Intent previousIntent;
        private List<Map<String, String>> messageHistory;
        private boolean initialized;

        public ConversationContext() {
            this.lastInteraction = LocalDateTime.now();
            this.currentIntent = Intent.UNKNOWN;
            this.messageHistory = new ArrayList<>();
            this.initialized = false;
        }

        public void updateLastInteraction() {
            this.lastInteraction = LocalDateTime.now();
        }

        public void updateIntent(Intent newIntent) {
            this.previousIntent = this.currentIntent;
            this.currentIntent = newIntent;
            updateLastInteraction();
        }

        public void clear() {
            selectedDate = null;
            selectedVetName = null;
            lastShownSchedule = null;
            isCheckingAvailability = false;
            currentIntent = Intent.UNKNOWN;
            previousIntent = null;
            lastInteraction = LocalDateTime.now();
            messageHistory.clear();
        }
    }

    /**
     * Obtiene un contexto de conversación para un usuario dado. Si no existe un contexto
     * asociado con el usuario, se crea uno nuevo.
     *
     * @param userId el identificador único del usuario para el que se desea obtener o crear
     *               un contexto de conversación.
     * @return el contexto de conversación asociado con el usuario especificado.
     */
    public ConversationContext getOrCreateContext(Long userId) {
        return userContexts.computeIfAbsent(userId, k -> new ConversationContext());
    }

    /**
     * Actualiza el contexto de conversación para un usuario específico.
     *
     * @param userId el identificador único del usuario cuyo contexto se va a actualizar.
     * @param context el contexto de conversación que contiene los datos actualizados
     *                que se asociarán al usuario.
     */
    public void updateContext(Long userId, ConversationContext context) {
        context.updateLastInteraction();
        userContexts.put(userId, context);
    }

    /**
     * Elimina el contexto de conversación asociado con un usuario específico.
     *
     * @param userId el identificador único del usuario cuyo contexto de conversación
     *               debe ser eliminado del sistema.
     */
    public void clearContext(Long userId) {
        userContexts.remove(userId);
    }

    /**
     * Elimina los contextos de conversación de usuarios que han permanecido inactivos
     * durante al menos una hora. Esta función recorre el conjunto de contextos almacenados
     * y remueve aquellos cuyo último registro de interacción es anterior al tiempo de corte,
     * que es fijado a una hora antes del momento actual.
     */
    public void cleanupOldContexts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        userContexts.entrySet().removeIf(entry ->
                entry.getValue().getLastInteraction().isBefore(cutoff));
    }
}