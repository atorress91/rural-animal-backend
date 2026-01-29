package com.project.demo.logic.entity.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Invocador de comandos que asocia intenciones (intents) con sus respectivos comandos.
 * Permite ejecutar comandos basados en la intención especificada.
 */
@Service
public class CommandInvoker {
    private final Map<Intent, ICommand> commands = new HashMap<>();
    private final GeminiClient geminiClient;
    private final ChatContextManager contextManager;

    /**
     * Constructor que registra los comandos específicos según la intención.
     *
     * @param vetAppointmentCommand comando para gestionar citas veterinarias.
     * @param auctionQueryCommand   comando para consultas de subastas.
     * @param petInfoCommand        comando para obtener información de animales.
     */
    @Autowired
    public CommandInvoker(
            VetAppointmentCommand vetAppointmentCommand,
            AuctionQueryCommand auctionQueryCommand,
            AnimalInfoCommand petInfoCommand,
            GeminiClient geminiClient,
            ChatContextManager contextManager
    ) {
        commands.put(Intent.VET_APPOINTMENT, vetAppointmentCommand);
        commands.put(Intent.AUCTION_QUERY, auctionQueryCommand);
        commands.put(Intent.ANIMAL_INFO, petInfoCommand);
        this.geminiClient = geminiClient;
        this.contextManager = contextManager;
    }

    /**
     * Ejecuta el comando asociado a la intención especificada.
     *
     * @param intent  la intención que determina el comando a ejecutar.
     * @param message el mensaje que será procesado por el comando.
     * @return el resultado de la ejecución del comando.
     * @throws IllegalArgumentException si no se encuentra un comando para la intención dada.
     */
    public String executeCommand(Intent intent, String message, Long userId) {
        ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);
        Intent previousIntent = context.getCurrentIntent();

        // Solo limpiar contexto si hay un cambio
        if (previousIntent != null && previousIntent != intent && previousIntent != Intent.UNKNOWN && intent != Intent.UNKNOWN) {
            // Verificar si es un cambio
            String lowerMessage = message.toLowerCase();
            boolean isExplicitChange = lowerMessage.contains("quiero") ||
                                       lowerMessage.contains("cambiar") ||
                                       lowerMessage.contains("ir a") ||
                                       lowerMessage.contains("mejor") ||
                                       lowerMessage.contains("vamos a");

            if (isExplicitChange) {
                String savedDate = String.valueOf(context.getSelectedDate());
                context.clear();

                if (intent == Intent.AUCTION_QUERY || intent == Intent.VET_APPOINTMENT) {
                    context.setSelectedDate(savedDate);
                }

                context.updateIntent(intent);
                contextManager.updateContext(userId, context);
            }
        }

        ICommand command = commands.get(intent);
        if (command == null || intent == Intent.UNKNOWN) {
            return geminiClient.processMessage(message, userId);
        }

        return command.execute(message, userId);
    }
}