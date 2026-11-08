package searchoteca.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/** Linha de GET /api/auditTrail. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuditLog(Long id,
                       @JsonProperty("time_stamp") LocalDateTime timeStamp,
                       String username, String action, String details) {}
