package searchoteca.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Documento vindo de GET /api/legal/termos ou /api/legal/privacidade (conteúdo em Markdown). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LegalDocument(String title, String version, String content) {}
