package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import searchoteca.frontend.dto.dashboard.DashboardCharts;
import searchoteca.frontend.dto.dashboard.DashboardData;
import searchoteca.frontend.service.BackendService;

import java.util.List;

/**
 * Dashboard administrativo (dashboard:view no backend; no menu, só root).
 * Os gráficos são SVG montados a partir das coordenadas que o DashboardCharts calcula.
 */
@Controller
public class DashboardController {

    /** Opções do seletor de período, em dias. */
    private static final List<Integer> PERIODS = List.of(7, 30, 90, 365);

    private final BackendService backendService;

    public DashboardController(BackendService backendService) {
        this.backendService = backendService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "30") int dias, Model model) {
        int period = PERIODS.contains(dias) ? dias : 30;

        DashboardData data = backendService.dashboard(period);
        if (data == null) data = DashboardData.empty(period);

        model.addAttribute("d", data);
        model.addAttribute("k", data.kpis());
        model.addAttribute("line", DashboardCharts.line(data.diary()));
        model.addAttribute("donut", DashboardCharts.donut(data.copiesByStatus()));
        model.addAttribute("periods", PERIODS);
        model.addAttribute("period", period);
        return "dashboard";
    }
}
