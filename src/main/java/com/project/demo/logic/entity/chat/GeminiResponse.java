package com.project.demo.logic.entity.chat;

import lombok.Data;
import java.util.List;

/**
 * Representa la respuesta de Gemini con una lista de candidatos.
 */
@Data
public class GeminiResponse {
    private List<Candidate> candidates;

    /**
     * Representa un candidato dentro de la respuesta de Gemini.
     */
    @Data
    public static class Candidate {
        private Content content;
    }

    /**
     * Representa el contenido dentro de un candidato.
     */
    @Data
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    /**
     * Representa una parte del contenido.
     */
    @Data
    public static class Part {
        private String text;
    }
}
