package searchoteca.frontend.dto.circulation;

import java.util.LinkedHashMap;
import java.util.Map;

/** Rótulos em português dos tipos de saída do backend (enum ExitType). */
public final class ExitTypes {
    private ExitTypes() {}

    /** Só as saídas definitivas: empréstimo tem tela própria. */
    public static final Map<String, String> DEFINITIVE = new LinkedHashMap<>();
    static {
        DEFINITIVE.put("SALE", "Venda");
        DEFINITIVE.put("DONATION", "Doação");
        DEFINITIVE.put("WRITE_OFF", "Baixa");
    }

    public static String label(String type) {
        if ("LOAN".equals(type)) return "Empréstimo";
        return DEFINITIVE.getOrDefault(type, type);
    }
}
