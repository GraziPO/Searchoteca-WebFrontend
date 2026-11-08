package searchoteca.frontend.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lê as listas digitadas ou lidas pelo leitor de código de barras.
 * O leitor USB "digita" o código e aperta Enter, então cada leitura vira uma linha;
 * também aceitamos vírgula, ponto e vírgula e espaço como separadores.
 */
final class FormParsing {
    private FormParsing() {}

    static List<String> codes(String text) {
        if (text == null) return List.of();
        return Arrays.stream(text.split("[\\s,;]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** @throws IllegalArgumentException com a mensagem para o usuário se algum valor não for número */
    static List<Long> ids(String text) {
        List<Long> ids = new ArrayList<>();
        for (String token : codes(text)) {
            try {
                ids.add(Long.parseLong(token));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("\"" + token + "\" não é um ID de exemplar válido. "
                        + "Use os números de ID impressos nas etiquetas.");
            }
        }
        return ids;
    }
}
