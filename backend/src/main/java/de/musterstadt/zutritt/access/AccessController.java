package de.musterstadt.zutritt.access;

import de.musterstadt.zutritt.access.dto.AccessAttemptResponse;
import de.musterstadt.zutritt.access.dto.AccessCheckRequest;
import de.musterstadt.zutritt.access.dto.AccessCheckResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AccessController {

    private final AccessCheckService accessCheckService;
    private final AccessAttemptService accessAttemptService;

    public AccessController(AccessCheckService accessCheckService, AccessAttemptService accessAttemptService) {
        this.accessCheckService = accessCheckService;
        this.accessAttemptService = accessAttemptService;
    }

    /** 201: every check creates a new access_attempt row. */
    @PostMapping("/access-checks")
    @ResponseStatus(HttpStatus.CREATED)
    public AccessCheckResponse check(@Valid @RequestBody AccessCheckRequest request) {
        return accessCheckService.check(request.playerId());
    }

    /** Without "date" the current day in Europe/Berlin is used. */
    @GetMapping("/access-attempts")
    public List<AccessAttemptResponse> findByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return accessAttemptService.findByDate(date != null ? date : accessAttemptService.today());
    }
}
