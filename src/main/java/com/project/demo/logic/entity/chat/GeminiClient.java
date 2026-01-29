package com.project.demo.logic.entity.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cliente para interactuar con la API de Gemini, manejando el flujo de mensajes de chat y el historial de mensajes.
 */
@Service
@Slf4j
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private final WebClient webClient;
    private final ChatContextManager contextManager;

    /**
     * Constructor que inicializa el cliente WebClient para realizar solicitudes a la API de Gemini.
     *
     * @param webClientBuilder constructor de WebClient para configurar la conexión.
     * @param contextManager gestor del contexto de conversación.
     */
    public GeminiClient(WebClient.Builder webClientBuilder, ChatContextManager contextManager) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build();
        this.contextManager = contextManager;
    }

    /**
     * Inicia el chat con un mensaje de bienvenida y opciones.
     *
     * @return la respuesta inicial del asistente.
     */
    public String startChat() {
        String systemInstruction = """
                Eres el asistente Rural Animal, un asistente amigable y profesional especializado en servicios veterinarios.
                Al iniciar, saluda cordialmente y presenta las siguientes opciones:
                1. Consultar sobre un animal,
                2. Revisar subastas activas,
                3. Crear una cita veterinaria,
                
                Para las citas veterinarias, debes:
                - Guiar al usuario paso a paso en el proceso de creación de citas
                - Aceptar comandos como 'ver disponibilidad para YYYY-MM-DD' para mostrar horarios disponibles
                - Aceptar comandos como 'crear cita para el YYYY-MM-DD HH:mm con el nombre del veterinario' para crear citas
                - Responder con VET_APPOINTMENT cuando el usuario quiera gestionar citas
                - Mantener un tono profesional pero amigable
                - Si el usuario solicita información de disponibilidad, pedir la fecha específica en formato YYYY-MM-DD o también fecha completa ejemplo: 12 de Diciembre
                - Si el usuario quiere crear una cita, solicitar la fecha, hora y el nombre del veterinario si no los proporciona
                
                Para las demás opciones:
                - Responder con ANIMAL_INFO para consultas sobre animales
                - Responder con AUCTION_QUERY para consultas sobre subastas
                """;

        String initialMessage = "Iniciando chat";

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(createContent("user", initialMessage));

        return sendMessageToGemini(contents, systemInstruction);
    }

    /**
     * Procesa un mensaje del usuario y obtiene la respuesta del asistente.
     *
     * @param message el mensaje del usuario.
     * @param userId el ID del usuario.
     * @return la respuesta generada por el asistente.
     */
    public String processMessage(String message, Long userId) {
        ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);
        List<Map<String, String>> openAIHistory = context.getMessageHistory();

        String systemInstruction = """
                Eres el asistente Rural Animal, especializado en servicios veterinarios.
                Mantén el contexto de la conversación y responde acorde al tema actual.
                Solo muestra el menú principal cuando el usuario lo solicite específicamente
                o cuando se desvía completamente del contexto actual.
                """;

        // Convertir historial de OpenAI a formato Gemini
        List<Map<String, Object>> geminiContents = convertHistoryToGeminiFormat(openAIHistory);
        geminiContents.add(createContent("user", message));

        String response = sendMessageToGemini(geminiContents, systemInstruction);

        // Guardar en formato compatible con el historial existente
        openAIHistory.add(Map.of("role", "user", "content", message));
        openAIHistory.add(Map.of("role", "assistant", "content", response));

        context.setMessageHistory(openAIHistory);
        contextManager.updateContext(userId, context);

        return response;
    }

    /**
     * Analiza el intent del mensaje proporcionado utilizando Gemini.
     *
     * @param message El mensaje del usuario que se requiere analizar para determinar su intención.
     * @param userId el ID del usuario.
     * @return Un objeto Intent que representa el código de intención analizado.
     */
    public Intent analyzeIntent(String message, Long userId) {
        ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);
        Intent currentIntent = context.getCurrentIntent();

        String systemInstruction = """
                Eres un analizador de intenciones para un sistema veterinario.
                
                CONTEXTO ACTUAL: %s
                
                REGLAS IMPORTANTES:
                1. Si el contexto actual es ANIMAL_INFO y el usuario pregunta sobre animales, especies, razas, disponibilidad de animales, vacunos, bovinos, equinos, gallinas, etc. -> MANTENER ANIMAL_INFO
                2. Si el contexto actual es AUCTION_QUERY y el usuario pregunta sobre subastas, precios, pujas, fechas de subastas -> MANTENER AUCTION_QUERY  
                3. Si el contexto actual es VET_APPOINTMENT y el usuario pregunta sobre citas, horarios, veterinarios, disponibilidad de doctores -> MANTENER VET_APPOINTMENT
                4. SOLO cambia de intent si el usuario EXPLÍCITAMENTE pide cambiar de tema (ej: "quiero ver subastas", "mejor vamos a citas", "cambiemos a...")
                
                CLASIFICACIÓN:
                - ANIMAL_INFO: Preguntas sobre animales, especies, razas, características, "¿hay vacunos?", "busco gallinas", "¿tienen caballos?", disponibilidad de animales
                - AUCTION_QUERY: Preguntas ESPECÍFICAS sobre subastas, "¿qué subastas hay?", "subastas para mañana", pujas, remates
                - VET_APPOINTMENT: Citas veterinarias, horarios de doctores, agendar consulta, disponibilidad de veterinarios
                - UNKNOWN: Saludos, despedidas, preguntas generales no relacionadas
                
                IMPORTANTE: 
                - La palabra "disponible" o "disponibilidad" en contexto de ANIMALES significa ANIMAL_INFO, NO subastas
                - Si el usuario dice "¿hay X disponibles?" donde X es un animal -> ANIMAL_INFO
                - Solo responde con el código, sin explicaciones
                """.formatted(currentIntent != null ? currentIntent.name() : "NINGUNO");

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(createContent("user", "Contexto: " + (currentIntent != null ? currentIntent.name() : "NINGUNO") + "\nMensaje del usuario: " + message));

        String responseContent = sendMessageToGemini(contents, systemInstruction);
        Intent newIntent = parseIntent(responseContent);

        // Si el contexto actual no es UNKNOWN y el nuevo intent es diferente,
        // verificar si realmente debería cambiar
        if (currentIntent != null && currentIntent != Intent.UNKNOWN && newIntent != currentIntent) {
            // Solo permitir cambio si es explícito
            String lowerMessage = message.toLowerCase();
            boolean explicitChange = lowerMessage.contains("quiero ver subasta") ||
                                    lowerMessage.contains("ir a subasta") ||
                                    lowerMessage.contains("cambiar a") ||
                                    lowerMessage.contains("mejor vamos a") ||
                                    lowerMessage.contains("quiero una cita") ||
                                    lowerMessage.contains("agendar cita") ||
                                    lowerMessage.contains("consultar animal") ||
                                    lowerMessage.contains("buscar animal") ||
                                    (lowerMessage.contains("subasta") && !currentIntent.equals(Intent.ANIMAL_INFO));

            if (!explicitChange && currentIntent == Intent.ANIMAL_INFO) {
                // Mantener ANIMAL_INFO si está preguntando sobre animales
                if (lowerMessage.contains("disponible") || lowerMessage.contains("hay") ||
                    lowerMessage.contains("tienen") || lowerMessage.contains("busco")) {
                    newIntent = Intent.ANIMAL_INFO;
                }
            }
        }

        context.updateIntent(newIntent);
        contextManager.updateContext(userId, context);

        return newIntent;
    }

    /**
     * Crea un objeto de contenido para Gemini.
     */
    private Map<String, Object> createContent(String role, String text) {
        return Map.of(
                "role", role,
                "parts", List.of(Map.of("text", text))
        );
    }

    /**
     * Convierte el historial de mensajes del formato OpenAI al formato Gemini.
     */
    private List<Map<String, Object>> convertHistoryToGeminiFormat(List<Map<String, String>> openAIHistory) {
        List<Map<String, Object>> geminiContents = new ArrayList<>();

        for (Map<String, String> msg : openAIHistory) {
            String role = msg.get("role");
            String content = msg.get("content");

            // Gemini usa "user" y "model" en lugar de "user" y "assistant"
            // Ignoramos mensajes de "system" ya que Gemini los maneja diferente
            if ("system".equals(role)) {
                continue;
            }

            String geminiRole = "assistant".equals(role) ? "model" : role;
            geminiContents.add(createContent(geminiRole, content));
        }

        return geminiContents;
    }

    /**
     * Envía los mensajes al API de Gemini y obtiene la respuesta.
     *
     * @param contents el historial de contenidos a enviar.
     * @param systemInstruction instrucciones del sistema.
     * @return el contenido de la respuesta generada por Gemini.
     * @throws RuntimeException si ocurre un error en la solicitud.
     */
    private String sendMessageToGemini(List<Map<String, Object>> contents, String systemInstruction) {
        log.info("Sending request to Gemini API with model: {}", model);

        Map<String, Object> requestBody = Map.of(
                "contents", contents,
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024
                )
        );

        try {
            GeminiResponse response = webClient.post()
                    .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                            .jitter(0.75)
                            .filter(throwable -> {
                                if (throwable instanceof WebClientResponseException e) {
                                    return e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
                                }
                                return false;
                            })
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                    new RuntimeException("Failed after 3 retries", retrySignal.failure())
                            )
                    )
                    .block();

            assert response != null;
            return response.getCandidates().getFirst().getContent().getParts().getFirst().getText();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("You have exceeded the allowed request limit. Please try again later.", e);
            } else {
                log.error("Error calling Gemini API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            }
            throw new RuntimeException("Unexpected error processing the request", e);
        }
    }

    /**
     * Analiza el contenido de una respuesta para determinar un intento específico.
     *
     * @param responseContent El contenido de la respuesta que se debe analizar.
     * @return Un objeto de tipo Intent que representa el intento identificado.
     */
    private Intent parseIntent(String responseContent) {
        try {
            String cleanResponse = responseContent.trim()
                    .split("\n")[0]
                    .replaceAll("[^A-Z_]", "");
            return Intent.valueOf(cleanResponse);
        } catch (IllegalArgumentException e) {
            log.error("Invalid intent received: {}", responseContent);
            return Intent.UNKNOWN;
        }
    }

    /**
     * Reinicia el historial de mensajes del chat.
     *
     * @param userId el ID del usuario.
     */
    public void resetMessageHistory(Long userId) {
        ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);
        context.getMessageHistory().clear();
        contextManager.updateContext(userId, context);
    }
}
