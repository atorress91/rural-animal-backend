package com.project.demo.logic.entity.chat;

import com.project.demo.logic.entity.veterinaryAppointment.CreateAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
public class VetAppointmentCommand implements ICommand {

    private final VeterinaryAppointmentService appointmentService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final GeminiClient geminiClient;
    private final ChatContextManager contextManager;

    public VetAppointmentCommand(
            VeterinaryAppointmentService appointmentService,
            GeminiClient geminiClient,
            ChatContextManager contextManager) {
        this.appointmentService = appointmentService;
        this.geminiClient = geminiClient;
        this.contextManager = contextManager;
    }

    /**
     * Ejecuta el comando para procesar un mensaje relacionado con consultas de disponibilidad
     * o creación de citas veterinarias. El método analiza el mensaje del usuario utilizando
     * inteligencia artificial para determinar la acción a seguir.
     *
     * @param message el mensaje de entrada proporcionado por el usuario, que contiene
     *                una consulta de disponibilidad o una solicitud de cita.
     * @param userId el identificador único del usuario que envía el mensaje, utilizado
     *               para gestionar el contexto de la conversación.
     * @return un mensaje de respuesta que indica la acción realizada o instrucciones
     *         adicionales para el usuario, según el análisis del mensaje proporcionado.
     */
    @Override
    public String execute(String message, Long userId) {
        try {
            ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);
            LocalDateTime now = LocalDateTime.now();
            String todayStr = now.toLocalDate().toString();

            String aiPrompt = """
                    Fecha actual: %s
                    
                    Analiza este mensaje y extrae:
                    1. Si es una consulta de disponibilidad (responde solo: true o false)
                    2. La fecha mencionada
                    3. El nombre del veterinario (extrae el nombre completo o parcial mencionado, si no hay responde NINGUNO)
                    
                    Contexto actual:
                    - Fecha previamente mencionada: %s
                    - Veterinario previamente mencionado: %s
                    
                    IMPORTANTE para fechas relativas:
                    - "mañana" = %s
                    - "pasado mañana" = %s
                    - "hoy" = %s
                    - Para "próximo viernes" calcula la fecha correcta
                    
                    Responde exactamente en este formato:
                    TIPO: DISPONIBILIDAD/CITA
                    FECHA: [fecha en formato YYYY-MM-DD, o YYYY-MM-DD HH:mm si incluye hora]
                    VETERINARIO: [nombre extraído o NINGUNO]
                    
                    Mensaje: %s""".formatted(
                    todayStr,
                    context.getSelectedDate() != null ? context.getSelectedDate() : "NINGUNA",
                    context.getSelectedVetName() != null ? context.getSelectedVetName() : "NINGUNO",
                    now.plusDays(1).toLocalDate().toString(),
                    now.plusDays(2).toLocalDate().toString(),
                    todayStr,
                    message);

            String aiResponse = geminiClient.processMessage(aiPrompt,userId);

            boolean isAvailability = aiResponse.contains("TIPO: DISPONIBILIDAD");
            String extractedDate = extractValue(aiResponse, "FECHA:");
            String extractedVetName = extractValue(aiResponse, "VETERINARIO:");

            if (!extractedDate.equals("NINGUNA")) {
                context.setSelectedDate(extractedDate);
            }
            if (!extractedVetName.equals("NINGUNO")) {
                context.setSelectedVetName(extractedVetName);
            }
            context.setCheckingAvailability(isAvailability);
            contextManager.updateContext(userId, context);

            if (isAvailability) {
                String dateToUse = extractedDate.equals("NINGUNA") ? context.getSelectedDate() : extractedDate;
                if (dateToUse != null) {
                    return handleAvailabilityCheck(dateToUse);
                }
            } else if (context.getSelectedVetName() != null) {
                String dateToUse = extractedDate.equals("NINGUNA") ? context.getSelectedDate() : extractedDate;
                if (dateToUse != null) {
                    return handleAppointmentCreation(dateToUse, context.getSelectedVetName(), userId);
                }
            }

            if (context.isCheckingAvailability()) {
                return """
                    Puedes darme la fecha en formato:
                    
                    1 - YYYY-MM-DD HH:mm
                    2 - Fecha completa, ejemplo: 12 de Diciembre del 2024\"""";
            }

            return """
                    ¿Cómo puedo ayudarte?
                    
                    Puedes:
                    • Consultar disponibilidad para una fecha
                    • Agendar una cita especificando fecha, hora y doctor
                    
                    Por ejemplo:
                    - "¿Qué horarios hay disponibles mañana?"
                    - "Quiero una cita con el doctor Oscar Brenes\"""";

        } catch (Exception e) {
            log.error("Error procesando comando de cita", e);
            return "Lo siento, hubo un error al procesar su solicitud. Por favor, inténtelo de nuevo.";
        }
    }

    /**
     * Extrae un valor específico de una cadena de respuesta que se encuentra
     * precedido por una clave dada. La búsqueda se realiza línea por línea dentro
     * de la cadena de entrada.
     *
     * @param response la cadena de respuesta completa de la cual se desea extraer
     *                 el valor asociado a la clave.
     * @param key      la clave que indica el inicio del valor que se quiere extraer.
     * @return el valor asociado a la clave si se encuentra, o "NINGUNA" si no se
     *         encuentra la clave en la respuesta.
     */
    private String extractValue(String response, String key) {
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.startsWith(key)) {
                return line.substring(key.length()).trim();
            }
        }
        return "NINGUNA";
    }

    /**
     * Maneja la verificación de disponibilidad de citas para una fecha específica proporcionada en formato de cadena.
     *
     * @param dateStr La cadena que representa la fecha para la cual se desea verificar la disponibilidad de citas.
     *                El formato esperado es "yyyy-MM-dd".
     * @return Una cadena que contiene los horarios y veterinarios disponibles para la fecha específica si hay disponibilidad,
     *         o un mensaje notificando la falta de disponibilidades para esa fecha. Si el formato de la fecha es incorrecto,
     *         devuelve un mensaje notificando la invalidez de la fecha.
     */
    private String handleAvailabilityCheck(String dateStr) {
        try {
            LocalDateTime startDate;
            LocalDate today = LocalDate.now();

            // Manejar fechas relativas
            String lowerDateStr = dateStr.toLowerCase().trim();
            if (lowerDateStr.equals("mañana") || lowerDateStr.equals("manana")) {
                startDate = today.plusDays(1).atStartOfDay();
            } else if (lowerDateStr.equals("hoy")) {
                startDate = today.atStartOfDay();
            } else if (lowerDateStr.equals("pasado mañana") || lowerDateStr.equals("pasado manana")) {
                startDate = today.plusDays(2).atStartOfDay();
            } else if (lowerDateStr.contains("próximo") || lowerDateStr.contains("proximo")) {
                LocalDate dayOfWeek = parseDayOfWeek(lowerDateStr, today);
                startDate = dayOfWeek != null ? dayOfWeek.atStartOfDay() : LocalDateTime.parse(dateStr + " 00:00", DATE_FORMATTER);
            } else {
                // Intentar parsear formato estándar
                try {
                    startDate = LocalDateTime.parse(dateStr + " 00:00", DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    // Si falla, intentar solo con la fecha
                    startDate = LocalDate.parse(dateStr).atStartOfDay();
                }
            }

            final LocalDateTime finalStartDate = startDate;
            LocalDateTime endDate = startDate.plusDays(1);
            String displayDate = startDate.toLocalDate().toString();

            var availabilities = appointmentService.getAvailableDates(startDate, endDate);

            if (availabilities.isEmpty()) {
                return "Lo siento, no hay horarios disponibles para el " + displayDate + ".\n" +
                        "¿Te gustaría consultar otra fecha?";
            }

            StringBuilder response = new StringBuilder();
            response.append("📅 Horarios disponibles para el ").append(displayDate).append(":\n\n");

            availabilities.stream()
                    .filter(availability -> availability.getDate().toLocalDate().equals(finalStartDate.toLocalDate()))
                    .findFirst()
                    .ifPresent(availability -> availability.getAvailableSlots().forEach(slot -> {
                        response.append("🕒 Horario: ").append(slot.getStartTime().format(DATE_FORMATTER))
                                .append(" - ")
                                .append(slot.getEndTime().format(DATE_FORMATTER))
                                .append("\n\n");

                        response.append("Veterinarios disponibles:\n");
                        slot.getAvailableVeterinarians().forEach(vet -> response.append("👨‍⚕️ Dr. ").append(vet.getName())
                                .append(" ").append(vet.getLastName1())
                                .append(" (").append(vet.getSpeciality()).append(")\n"));
                        response.append("\n");
                    }));

            response.append("\nPara agendar, menciona el nombre del veterinario y el horario que prefieres.");
            return response.toString();

        } catch (DateTimeParseException e) {
            return "La fecha proporcionada no es válida. Por favor, intenta de nuevo.";
        }
    }

    /**
     * Método para manejar la creación de una cita veterinaria.
     *
     * @param dateTimeStr La fecha y hora de la cita en formato de cadena. Debe seguir el formato 'YYYY-MM-DD HH:mm'.
     * @param vetName El nombre del veterinario con el que se desea agendar la cita.
     * @param userId El identificador único del usuario que solicita la creación de la cita.
     * @return Un mensaje indicando si la cita fue agendada exitosamente o si ocurrió un error durante el proceso. El mensaje también puede solicitar información adicional si el veterin
     * ario no es identificado.
     */
    @Transactional
    protected String handleAppointmentCreation(String dateTimeStr, String vetName, Long userId) {
        try {

            LocalDateTime startDateTime;
            try {
                startDateTime = LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                String aiDatePrompt = String.format("""
                        Convierte esta fecha/hora a formato YYYY-MM-DD HH:mm: %s
                        Instrucciones:
                        - Acepta fechas en formato natural (ej: "3 de diciembre del 2024 17:00")
                        - Convierte a formato "2024-12-03 17:00"
                        - Asegúrate de usar dos dígitos para mes, día y hora
                        Responde SOLO con la fecha en ese formato, nada más.
                        """, dateTimeStr);
                String formattedDate = geminiClient.processMessage(aiDatePrompt,userId).trim();
                startDateTime = LocalDateTime.parse(formattedDate, DATE_FORMATTER);
            }
            LocalDateTime endDateTime = startDateTime.plusMinutes(30);

            String vetSearchPrompt = String.format("""
                    A partir del nombre del veterinario: "%s"
                    Busca su ID correspondiente en la siguiente lista:
                    %s
                    
                    **Instrucciones para la búsqueda:**
                    - Ignora acentos y mayúsculas/minúsculas
                    - Busca coincidencias parciales del nombre o apellido
                    - Si el nombre proporcionado coincide parcialmente con un veterinario, considera que es una coincidencia
                    - Si hay múltiples coincidencias, elige la más exacta
                    - Si no hay coincidencia, responde exactamente: NO_MATCH
                    
                    Ejemplos de coincidencias válidas:
                    - "Carolina" coincide con "Dr. Carolina Méndez"
                    - "Mendez" coincide con "Dr. Carolina Méndez"
                    - "carolina mendez" coincide con "Dr. Carolina Méndez"
                    """, vetName, getAvailableVetsForDateTime(startDateTime));


            String vetIdResponse = geminiClient.processMessage(vetSearchPrompt,userId).trim();

            vetIdResponse = vetIdResponse.trim();
            if (vetIdResponse.equalsIgnoreCase("NO_MATCH")) {
                return "No pude encontrar al veterinario '" + vetName + "'. Por favor, especifica el nombre completo " +
                        "o elige uno de la lista:\n\n" + getAvailableVetsForDateTime(startDateTime);
            }

            try {
                vetIdResponse = vetIdResponse.replaceAll("\\D", "");
                Long vetId = Long.parseLong(vetIdResponse);
                CreateAppointmentDto appointmentDTO = new CreateAppointmentDto();
                appointmentDTO.setVeterinaryId(vetId);
                appointmentDTO.setStartDate(startDateTime);
                appointmentDTO.setEndDate(endDateTime);

                VeterinaryAppointmentDto createdAppointment = appointmentService.createAppointment(appointmentDTO, userId);

                this.contextManager.clearContext(userId);
                return String.format(
                        """
                                ✅ ¡Cita agendada exitosamente!
                                
                                📅 Fecha: %s
                                👨‍⚕️ Veterinario: Dr. %s %s
                                🏥 Especialidad: %s
                                
                                ¿Necesitas algo más?""",
                        createdAppointment.getStartDate().format(DATE_FORMATTER),
                        createdAppointment.getVeterinaryName(),
                        createdAppointment.getFirstSurname(),
                        createdAppointment.getSpeciality()
                );
            } catch (NumberFormatException e) {
                log.error("Error al parsear el ID del veterinario: {}", vetIdResponse, e);
                return "Parece que hubo un error al identificar al veterinario. Por favor, verifica que hayas ingresado el nombre correctamente o utiliza el ID de la lista.";
            }

        } catch (DateTimeParseException e) {
            return "Por favor, especifica la fecha y hora en formato YYYY-MM-DD HH:mm\n" +
                    "Por ejemplo: 2024-12-02 09:00";
        } catch (Exception e) {
            log.error("Error al crear la cita", e);
            return "Lo siento, ocurrió un error al agendar la cita: " + e.getMessage();
        }
    }

    /**
     * Obtiene información de los veterinarios disponibles para una fecha y hora específicas.
     *
     * @param dateTime La fecha y hora para la cual se desea consultar la disponibilidad de los veterinarios.
     * @return Una cadena con la información de los veterinarios disponibles en el formato
     *         "ID: {id} - Dr. {nombre} {apellido} ({especialidad})", o una cadena vacía
     *         si no hay disponibilidad.
     */
    private String getAvailableVetsForDateTime(LocalDateTime dateTime) {
        StringBuilder vetsInfo = new StringBuilder();
        var availabilities = appointmentService.getAvailableDates(dateTime, dateTime.plusMinutes(30));

        if (!availabilities.isEmpty()) {
            availabilities.getFirst().getAvailableSlots().stream()
                    .filter(slot -> slot.getStartTime().equals(dateTime))
                    .findFirst()
                    .ifPresent(slot -> slot.getAvailableVeterinarians().forEach(vet -> vetsInfo.append(String.format("ID: %d - Dr. %s %s (%s)\n",
                            vet.getId(),
                            vet.getName(),
                            vet.getLastName1(),
                            vet.getSpeciality()))));
        }

        return vetsInfo.toString();
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
}