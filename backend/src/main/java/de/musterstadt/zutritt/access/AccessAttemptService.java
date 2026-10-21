package de.musterstadt.zutritt.access;

import de.musterstadt.zutritt.access.dto.AccessAttemptResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class AccessAttemptService {

    private final AccessAttemptRepository accessAttemptRepository;
    private final Clock clock;

    public AccessAttemptService(AccessAttemptRepository accessAttemptRepository, Clock clock) {
        this.accessAttemptRepository = accessAttemptRepository;
        this.clock = clock;
    }

    /**
     * All attempts of one calendar day in the business zone (Europe/Berlin).
     * The day is converted to an Instant range [00:00, next day 00:00), which also handles 23h/25h DST days.
     */
    @Transactional(readOnly = true)
    public List<AccessAttemptResponse> findByDate(LocalDate date) {
        Instant from = date.atStartOfDay(clock.getZone()).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        return accessAttemptRepository.findInRange(from, to).stream()
                .map(AccessAttemptResponse::from)
                .toList();
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }
}
