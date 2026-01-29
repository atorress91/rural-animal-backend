package com.project.demo.logic.entity.chat;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnimalInfoCommand implements ICommand {

    private final TblPublicationRepository publicationRepository;
    private final GeminiClient geminiClient;
    private final ChatContextManager contextManager;

    public AnimalInfoCommand(TblPublicationRepository publicationRepository, GeminiClient geminiClient, ChatContextManager contextManager) {
        this.publicationRepository = publicationRepository;
        this.geminiClient = geminiClient;
        this.contextManager = contextManager;
    }

    /**
     * Ejecuta el comando usando un mensaje proporcionado y el ID de usuario asociado.
     * Analiza el mensaje para identificar la intención de obtener información sobre animales,
     * determinando la especie y raza mencionadas, y procede con la búsqueda de coincidencias.
     *
     * @param message el mensaje de entrada que contiene potencialmente la consulta sobre información animal.
     * @param userId el ID del usuario que envió el mensaje, utilizado para gestionar el contexto de conversación.
     * @return el resultado de la ejecución del comando, que puede ser una respuesta inicial,
     *         una respuesta procesada o un mensaje de error si ocurre una excepción durante el procesamiento.
     */
    @Override
    public String execute(String message, Long userId) {
        try {
            if (message.toLowerCase().contains("información") || message.toLowerCase().contains("informacion")) {
                return getInitialResponse();
            }

            ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);
            context.updateIntent(Intent.ANIMAL_INFO);

            try {
                String aiPrompt = """
                        Como asistente de búsqueda de animales, analiza este mensaje:
                        "%s"
                        
                        Si te preguntan en plural convierte el resultado de la especie o raza a singular
                        
                        Extrae la siguiente información:
                        1. ¿Qué especie de animal se menciona? (si no hay ninguna, responde NINGUNA)
                        2. ¿Qué raza específica se menciona? (si no hay ninguna, responde NINGUNA)
                        
                        Responde EXACTAMENTE en este formato:
                        ESPECIE: [especie extraída o NINGUNA]
                        RAZA: [raza extraída o NINGUNA]
                        """.formatted(message);

                String aiResponse = geminiClient.processMessage(aiPrompt, userId);

                if (aiResponse == null || aiResponse.isEmpty()) {
                    return getInitialResponse();
                }

                String extractedSpecies = extractValue(aiResponse, "ESPECIE:");
                String extractedBreed = extractValue(aiResponse, "RAZA:");

                return searchAnimals(
                        extractedSpecies.equals("NINGUNA") ? null : extractedSpecies,
                        extractedBreed.equals("NINGUNA") ? null : extractedBreed
                );

            } catch (Exception e) {
                log.error("Error al procesar mensaje con OpenAI", e);
                return processMessageBasic(message);
            }

        } catch (Exception e) {
            log.error("Error procesando comando de búsqueda de animales", e);
            return "Lo siento, hubo un error al procesar su solicitud. ¿Podría reformular su pregunta?";
        }
    }

    /**
     * Procesa el mensaje de entrada para identificar la especie y raza comunes presentes en el
     * mensaje y realiza una búsqueda basada en estos parámetros.
     *
     * @param message El mensaje que se debe procesar para extraer la información relevante de especie
     *                y raza.
     * @return Un mensaje que es el resultado de la búsqueda de animales basada en la especie y raza
     *         identificadas, o un valor acorde si no se determina ninguna.
     */
    private String processMessageBasic(String message) {
        message = message.toLowerCase();
        String species = null;
        String breed = null;

        String[] commonSpecies = {"caprino", "bovino", "equino", "gallina", "ovino", "pato"};
        for (String s : commonSpecies) {
            if (message.contains(s)) {
                species = s;
                break;
            }
        }

        String[] commonBreeds = {"alpina", "gyr", "percherón", "leghorn", "dorper", "pato pekín"};
        for (String b : commonBreeds) {
            if (message.contains(b)) {
                breed = b;
                break;
            }
        }

        return searchAnimals(species, breed);
    }

    /**
     * Genera el mensaje de respuesta inicial para el usuario en el contexto.
     * Este mensaje proporciona pautas sobre cómo puede el usuario especificar el tipo de animal que
     * está buscando, ya sea por especie o raza.
     *
     * @return Un mensaje de bienvenida que incluye ejemplos sobre cómo buscar animales por especie
     *         o raza en el sistema.
     */
    private String getInitialResponse() {
        return """
                🔎 ¿Qué tipo de animal está buscando? Puede especificar:
                
                🐮 La especie (ejemplo: vacuno, ovino, equino)
                🦁 La raza específica (ejemplo: Brahman, Landrace, Holstein)
                
                Por ejemplo:
                - "🐄 ¿Hay vacunos disponibles?"
                - "🐔 Busco gallinas Leghorn"
                - "🐑 Quiero ver ovejas Dorper"
                """;
    }

    /**
     * Extrae un valor asociado a una clave específica de la respuesta proporcionada.
     * La respuesta está estructurada en líneas y la extracción se realiza buscando
     * la línea que comienza con la clave dada, devolviendo el valor a continuación
     * de la misma, luego de remover espacios en blanco.
     *
     * @param response la respuesta completa de donde se extraerá el valor.
     * @param key la clave que identifica la línea específica que contiene el valor.
     * @return el valor asociado a la clave, o "NINGUNA" si la clave no se encuentra en la respuesta.
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
     * Realiza una búsqueda de animales en base a la especie y la raza proporcionadas.
     * Si ambas entradas son nulas, proporciona una respuesta inicial.
     * Recupera publicaciones de ventas y subastas, y filtra los resultados según los
     * criterios de especie y raza. Genera un mensaje de respuesta con los animales
     * encontrados o un mensaje de error si ocurre un problema durante el proceso.
     *
     * @param species la especie de los animales que se desea buscar. Puede ser nulo.
     * @param breed la raza de los animales que se desea buscar. Puede ser nulo.
     * @return un mensaje con los resultados de la búsqueda de animales o un mensaje
     *         de error si ocurre algún problema durante el proceso.
     */
    private String searchAnimals(String species, String breed) {
        if (species == null && breed == null) {
            return getInitialResponse();
        }

        try {
            Pageable pageable = PageRequest.of(0, 10);

            Page<TblPublication> filteredPublications = publicationRepository.findBySpeciesAndBreed(
                    species,
                    breed,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
            );

            if (filteredPublications.isEmpty()) {
                return String.format("""
                                ❌ Lo siento, no encontré %s%s disponibles en este momento. \
                                
                                
                                🔄 ¿Te gustaría buscar otra especie o raza?""",
                        breed != null ? "animales de raza " + breed + " " : "",
                        species != null ? "de la especie " + species : "animales");
            }

            StringBuilder response = new StringBuilder();
            response.append("📋 Encontré los siguientes animales:\n\n");

            filteredPublications.forEach(pub -> {
                response.append(String.format("""
                                🔍 %s
                                🦮 Especie: %s
                                🐾 Raza: %s
                                ⚥ Género: %s
                                ⚖️ Peso: %d kg
                                🏷️ Tipo: %s
                                💰 Precio: %s
                                
                                """,
                        pub.getTitle(),
                        pub.getSpecie(),
                        pub.getRace(),
                        pub.getGender(),
                        pub.getWeight(),
                        pub.getType(),
                        pub.getType().equals("Subasta") ?
                                "Precio base: ₡" + pub.getPrice() + " (⬆️ Incremento mínimo: ₡" + pub.getMinimumIncrease() + ")" :
                                "₡" + pub.getPrice()
                ));
            });

            response.append("\n¿Te gustaría ver más detalles de alguno de estos animales?");
            return response.toString();

        } catch (Exception e) {
            log.error("Error al buscar animales en la base de datos", e);
            return "Lo siento, hubo un error al buscar los animales. Por favor, inténtelo de nuevo.";
        }
    }
}