package lidosunset.controller;

import lidosunset.model.dto.DashboardDatiDto;
import lidosunset.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Il controller espone gli indicatori chiave di presentazione (incassi, spese
// margine, urgenze). Può essere utilizzato solo dall'ADMIN.
// L'annotazione CrossOrigin viene utilizzato per autorizzare il browser
// a far comunicare il frontend con il backend

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    // Restituisce il riepilogo della dashboard al momento della richiesta
    @GetMapping("/kpi")
    public ResponseEntity<DashboardDatiDto> getDashboardKpi(){
        return ResponseEntity.ok(adminDashboardService.generaReportGiornaliero());
    }
}
