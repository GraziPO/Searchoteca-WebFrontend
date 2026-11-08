package searchoteca.frontend.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * O que cada perfil vê no menu. Espelha o permissions.sql do backend:
 * esconder um link aqui é só conveniência; quem decide de verdade é o @PreAuthorize do backend,
 * que responde 403 se alguém digitar a URL na mão.
 */
public record RoleAccess(String role,
                         boolean users,          // users:*
                         boolean archive,        // org_books/org_depart/org_local:view
                         boolean manageArchive,  // criar/editar/excluir + copies:*
                         boolean loans,          // loans:*
                         boolean exits,          // exits:create e receipts:view
                         boolean audit,          // audit:view
                         boolean editPrice,      // org_books:edit_price
                         boolean student) {      // loans:view_own

    public static RoleAccess of(String role) {
        String r = role == null ? "" : role;
        boolean root = r.equals("root");
        return new RoleAccess(r,
                root || r.equals("admin_users"),
                root || r.equals("warehouse_op") || r.equals("loan_op"),
                root || r.equals("warehouse_op"),
                root || r.equals("loan_op"),
                root || r.equals("loan_op"),
                root,
                root,
                r.equals("students"));
    }

    public boolean isRoot() {
        return "root".equals(role);
    }

    /** Página inicial depois do login. */
    public String homePath() {
        if (student) return "/meus-emprestimos";
        if (role.equals("loan_op")) return "/emprestimos";
        if (role.equals("admin_users")) return "/usuarios";
        return "/acervo";
    }

    /** Perfis do seed (permissions.sql), na ordem em que aparecem no cadastro. */
    public static final Map<String, String> LABELS = new LinkedHashMap<>();
    static {
        LABELS.put("root", "Root (acesso total)");
        LABELS.put("admin_users", "Administrador de usuários");
        LABELS.put("warehouse_op", "Operador de acervo");
        LABELS.put("loan_op", "Operador de empréstimo");
        LABELS.put("students", "Aluno");
    }

    public static String label(String role) {
        if (role == null) return "";
        return LABELS.getOrDefault(role, role);
    }
}
