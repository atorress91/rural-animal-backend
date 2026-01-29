package com.project.demo.logic.entity.chat;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


@Slf4j
@Component
public class AuctionQueryCommand implements ICommand {

    private final TblPublicationRepository publicationRepository;
    private final ChatContextManager contextManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final GeminiClient geminiClient;

    public AuctionQueryCommand(TblPublicationRepository publicationRepository,
                               ChatContextManager contextManager,
                               GeminiClient geminiClient) {
        this.publicationRepository = publicationRepository;
        this.contextManager = contextManager;
        this.geminiClient = geminiClient;
    }

    @Override
    public String execute(String message, Long userId) {
        try {
            ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);
            LocalDate today = LocalDate.now();

            String aiPrompt = """
                    Fecha actual: %s
                    
                    Analiza este mensaje y extrae:
                    1. Si es una consulta de subastas (responde solo: true o false)
                    2. La fecha mencionada (puede ser relativa como "mañana" o absoluta como "15 de enero")
                    
                    Contexto actual:
                    - Fecha previamente mencionada: %s
                    
                    IMPORTANTE para fechas relativas:
                    - "mañana" = %s
                    - "pasado mañana" = %s
                    - "hoy" = %s
                    - Para fechas como "15 de enero" usa formato YYYY-MM-DD
                    - Para "próximo viernes" calcula la fecha correcta
                    
                    Responde exactamente en este formato:
                    TIPO: CONSULTA/OTRO
                    FECHA: [fecha en formato YYYY-MM-DD o la palabra tal cual si es relativa como "mañana"]
                    
                    Mensaje: %s""".formatted(
                    today.format(DATE_FORMATTER),
                    context.getSelectedDate() != null ? context.getSelectedDate() : "NINGUNA",
                    today.plusDays(1).format(DATE_FORMATTER),
                    today.plusDays(2).format(DATE_FORMATTER),
                    today.format(DATE_FORMATTER),
                    message);

            String aiResponse = geminiClient.processMessage(aiPrompt, userId);

            boolean isQuery = aiResponse.contains("TIPO: CONSULTA");
            String extractedDate = extractValue(aiResponse, "FECHA:");

            if (!extractedDate.equals("NINGUNA")) {
                context.setSelectedDate(extractedDate);
            }
            contextManager.updateContext(userId, context);

            if (isQuery) {
                String dateToUse = extractedDate.equals("NINGUNA") ? context.getSelectedDate() : extractedDate;
                if (dateToUse != null) {
                    return handleAuctionQuery(dateToUse, userId);
                }
            }

            return """
                    ¿Cómo puedo ayudarte con las subastas?
                    
                    Puedes:
                    • Consultar subastas para una fecha específica
                    • Ver detalles de subastas activas
                    
                    Por ejemplo:
                    - "¿Qué subastas hay para mañana?"
                    - "¿Hay subastas programadas para el 15 de diciembre?"
                    - "Muestra las subastas del próximo viernes\"""";

        } catch (Exception e) {
            log.error("Error procesando consulta de subastas", e);
            return "Lo siento, hubo un error al procesar su solicitud. Por favor, inténtelo de nuevo.";
        }
    }

    private String extractValue(String response, String key) {
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.startsWith(key)) {
                return line.substring(key.length()).trim();
            }
        }
        return "NINGUNA";
    }

    private String handleAuctionQuery(String dateStr, Long userId) {
        try {
            LocalDate queryDate;
            LocalDate currentDate = LocalDate.now();

            try {
                queryDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                // Manejar fechas relativas primero
                String lowerDateStr = dateStr.toLowerCase().trim();
                if (lowerDateStr.equals("mañana") || lowerDateStr.equals("manana")) {
                    queryDate = currentDate.plusDays(1);
                } else if (lowerDateStr.equals("hoy")) {
                    queryDate = currentDate;
                } else if (lowerDateStr.equals("pasado mañana") || lowerDateStr.equals("pasado manana")) {
                    queryDate = currentDate.plusDays(2);
                } else if (lowerDateStr.contains("próximo") || lowerDateStr.contains("proximo")) {

                    queryDate = parseDayOfWeek(lowerDateStr, currentDate);
                    if (queryDate == null) {
                        // Usar IA para convertir
                        queryDate = convertDateWithAI(dateStr, currentDate, userId);
                    }
                } else {
                    // Usar IA para convertir fechas complejas
                    queryDate = convertDateWithAI(dateStr, currentDate, userId);
                }
            }

            if (queryDate.isBefore(currentDate)) {
                return "No se pueden consultar subastas para fechas pasadas. Por favor, ingresa una fecha actual o futura.";
            }

            Pageable pageable = PageRequest.of(0, 10);
            Page<TblPublication> auctions = publicationRepository.findActiveAuctionsByDate(
                    queryDate, pageable);

            if (auctions.isEmpty()) {
                return String.format("""
                                No hay subastas programadas para la fecha %s.
                                
                                Puedes:
                                • Consultar otra fecha
                                • Ver todas las subastas disponibles""",
                        queryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            StringBuilder response = new StringBuilder();
            response.append("📅 Subastas disponibles para ")
                    .append(queryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .append(":\n\n");

            auctions.getContent().forEach(auction -> {
                response.append("🔨 ").append(auction.getTitle()).append("\n");
                response.append("💰 Precio base: ₡").append(String.format("%,d", auction.getPrice())).append("\n");
                response.append("📈 Incremento mínimo: ₡").append(String.format("%,d", auction.getMinimumIncrease())).append("\n");
                response.append("⏰ Inicio: ").append(auction.getStartDate().format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
                response.append("🏁 Fin: ").append(auction.getEndDate().format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
                if (auction.getState() != null) {
                    response.append("📌 Estado: ").append(auction.getState()).append("\n");
                }
                response.append("\n");
            });

            if (auctions.getTotalPages() > 1) {
                response.append("\nPágina ").append(auctions.getNumber() + 1)
                        .append(" de ").append(auctions.getTotalPages());
            }

            response.append("\n\n¿Deseas ver más detalles de alguna subasta o consultar otra fecha?");
            return response.toString();

        } catch (DateTimeParseException e) {
            return """
                    La fecha proporcionada no es válida. Por favor, usa alguno de estos formatos:
                    • YYYY-MM-DD (ejemplo: 2024-12-25)
                    • Fecha natural (ejemplo: 25 de diciembre)
                    • Relativo (ejemplo: mañana, próximo viernes)""";
        }
    }

    /**
     * Parsea días de la semana relativos como "próximo viernes"
     */
    private LocalDate parseDayOfWeek(String dateStr, LocalDate currentDate) {
        String[] daysOfWeek = {"lunes", "martes", "miércoles", "miercoles", "jueves", "viernes", "sábado", "sabado", "domingo"};
        int[] dayValues = {1, 2, 3, 3, 4, 5, 6, 6, 7}; // DayOfWeek values (Monday = 1)

        for (int i = 0; i < daysOfWeek.length; i++) {
            if (dateStr.contains(daysOfWeek[i])) {
                int targetDay = dayValues[i];
                int currentDay = currentDate.getDayOfWeek().getValue();
                int daysToAdd = targetDay - currentDay;
                if (daysToAdd <= 0) {
                    daysToAdd += 7; // Siguiente semana
                }
                return currentDate.plusDays(daysToAdd);
            }
        }
        return null;
    }

    /**
     * Usa IA para convertir fechas complejas
     */
    private LocalDate convertDateWithAI(String dateStr, LocalDate currentDate, Long userId) {
        String aiDatePrompt = String.format("""
                Fecha actual: %s
                Convierte esta expresión de fecha a formato YYYY-MM-DD: "%s"
                
                Instrucciones:
                - "mañana" = fecha actual + 1 día
                - "pasado mañana" = fecha actual + 2 días  
                - "próximo viernes" = el viernes más cercano en el futuro
                - "15 de enero" = 2026-01-15 (año actual si no se especifica)
                - Usa el año actual (%d) si no se especifica
                
                Responde SOLO con la fecha en formato YYYY-MM-DD, nada más.
                """, currentDate.format(DATE_FORMATTER), dateStr, currentDate.getYear());

        String formattedDate = geminiClient.processMessage(aiDatePrompt, userId).trim();
        // Limpiar posibles caracteres extra
        formattedDate = formattedDate.replaceAll("[^0-9-]", "");
        return LocalDate.parse(formattedDate, DATE_FORMATTER);
    }
}