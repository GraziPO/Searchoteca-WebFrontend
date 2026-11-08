package searchoteca.frontend.dto.dashboard;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Transforma os números do dashboard em coordenadas de SVG, para o template só desenhar.
 * Assim a página não precisa de nenhuma biblioteca de gráfico em JavaScript.
 */
public final class DashboardCharts {

    // Área do gráfico de linhas (viewBox 0 0 640 220)
    public static final int W = 640, H = 220;
    private static final int PAD_LEFT = 36, PAD_RIGHT = 8, PAD_TOP = 10, PAD_BOTTOM = 24;

    // Rosca (viewBox 0 0 140 140)
    public static final double DONUT_R = 54;
    private static final double DONUT_C = 2 * Math.PI * DONUT_R;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");

    /** Nome em português e cor de cada CopyStatus. As cores diferem também em claridade. */
    private static final Map<String, String[]> STATUS = Map.of(
            "AVAILABLE",   new String[]{"Disponível", "#7A1620"},
            "LOANED",      new String[]{"Emprestado", "#E0955C"},
            "SOLD",        new String[]{"Vendido",    "#2F5D7C"},
            "DONATED",     new String[]{"Doado",      "#9DB7C9"},
            "WRITTEN_OFF", new String[]{"Baixado",    "#5E5452"});

    private DashboardCharts() {}

    // ------------------------------------------------------------------ linhas

    public record Tick(double y, long value) {}
    public record XLabel(double x, String text, String anchor) {}
    public record Peak(double x, double y, long value) {}

    public record LineChart(String loansLine, String loansArea, String returnsLine,
                            List<Tick> ticks, List<XLabel> labels, Peak peak,
                            long totalLoans, long totalReturns, boolean empty) {}

    public static LineChart line(List<DashboardData.Diary> days) {
        if (days == null || days.isEmpty()) {
            return new LineChart("", "", "", List.of(), List.of(), null, 0, 0, true);
        }
        int n = days.size();
        long max = 1;
        long totalLoans = 0, totalReturns = 0;
        for (DashboardData.Diary d : days) {
            max = Math.max(max, Math.max(d.loans(), d.returns()));
            totalLoans += d.loans();
            totalReturns += d.returns();
        }
        long top = niceCeil(max);

        double plotW = W - PAD_LEFT - PAD_RIGHT;
        double plotH = H - PAD_TOP - PAD_BOTTOM;
        double baseY = PAD_TOP + plotH;

        StringBuilder loans = new StringBuilder();
        StringBuilder returns = new StringBuilder();
        Peak peak = null;
        List<XLabel> labels = new ArrayList<>();
        int labelStep = Math.max(1, (int) Math.ceil((n - 1) / 4.0));

        for (int i = 0; i < n; i++) {
            DashboardData.Diary d = days.get(i);
            double x = n == 1 ? PAD_LEFT + plotW / 2 : PAD_LEFT + i * plotW / (n - 1);
            double yL = baseY - plotH * d.loans() / top;
            double yR = baseY - plotH * d.returns() / top;
            append(loans, x, yL);
            append(returns, x, yR);
            if (d.loans() > 0 && (peak == null || d.loans() > peak.value())) {
                peak = new Peak(round(x), round(yL), d.loans());
            }
            boolean last = i == n - 1;
            if (i % labelStep == 0 || last) {
                // evita o último rótulo encostar no penúltimo
                if (last && !labels.isEmpty() && x - labels.get(labels.size() - 1).x() < 60) {
                    labels.remove(labels.size() - 1);
                }
                String anchor = i == 0 ? "start" : last ? "end" : "middle";
                labels.add(new XLabel(round(x), d.day() == null ? "" : d.day().format(DAY), anchor));
            }
        }

        String area = String.format(Locale.ROOT, "%d,%.1f %s %.1f,%.1f",
                PAD_LEFT, baseY, loans.toString().trim(), W - PAD_RIGHT * 1.0, baseY);

        List<Tick> ticks = List.of(
                new Tick(round(baseY), 0),
                new Tick(round(baseY - plotH / 2), top / 2),
                new Tick(round(PAD_TOP), top));

        return new LineChart(loans.toString().trim(), area, returns.toString().trim(),
                ticks, labels, peak, totalLoans, totalReturns, totalLoans + totalReturns == 0);
    }

    // ------------------------------------------------------------------ rosca

    public record Slice(String label, String color, long quantity, int percent,
                        String dashArray, String dashOffset) {}

    public record Donut(List<Slice> slices, long total) {
        public boolean isEmpty() { return total == 0; }
    }

    public static Donut donut(List<DashboardData.CountByStatus> counts) {
        long total = 0;
        if (counts != null) for (DashboardData.CountByStatus c : counts) total += c.quantity();
        List<Slice> slices = new ArrayList<>();
        if (total == 0) return new Donut(slices, 0);

        double acc = 0;
        for (DashboardData.CountByStatus c : counts) {
            if (c.quantity() <= 0) continue;
            String[] meta = STATUS.getOrDefault(c.status(), new String[]{c.status(), "#B9A89E"});
            double len = DONUT_C * c.quantity() / total;
            slices.add(new Slice(meta[0], meta[1], c.quantity(),
                    (int) Math.round(c.quantity() * 100.0 / total),
                    String.format(Locale.ROOT, "%.2f %.2f", len, DONUT_C - len),
                    String.format(Locale.ROOT, "%.2f", -acc)));
            acc += len;
        }
        return new Donut(slices, total);
    }

    public static String statusLabel(String status) {
        String[] meta = STATUS.get(status);
        return meta == null ? status : meta[0];
    }

    // ------------------------------------------------------------------ util

    /** Arredonda o máximo do eixo para um número par e redondo (4, 6, 10, 20, 30...) para a escala ficar legível. */
    static long niceCeil(long max) {
        if (max <= 4) return 4;
        if (max <= 6) return 6;
        if (max <= 10) return 10;
        long magnitude = (long) Math.pow(10, Math.floor(Math.log10(max)));
        for (long step : new long[]{1, 2, 3, 4, 5, 6, 8, 10}) {
            long candidate = step * magnitude;
            if (candidate >= max) return candidate;
        }
        return 10 * magnitude;
    }

    private static void append(StringBuilder sb, double x, double y) {
        sb.append(String.format(Locale.ROOT, "%.1f,%.1f ", x, y));
    }

    private static double round(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
