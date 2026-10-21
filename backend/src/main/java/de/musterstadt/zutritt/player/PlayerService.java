package de.musterstadt.zutritt.player;

import de.musterstadt.zutritt.common.PlayerNotFoundException;
import de.musterstadt.zutritt.player.dto.CreatePlayerRequest;
import de.musterstadt.zutritt.player.dto.PlayerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final Clock clock;

    public PlayerService(PlayerRepository playerRepository, Clock clock) {
        this.playerRepository = playerRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> findAll() {
        return playerRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(PlayerResponse::from)
                .toList();
    }

    @Transactional
    public PlayerResponse create(CreatePlayerRequest request) {
        Player player = new Player(request.firstName().strip(), request.lastName().strip(),
                request.birthDate(), clock.instant());
        return PlayerResponse.from(playerRepository.save(player));
    }

    /** No explicit save(): the entity is "managed", Hibernate writes the change at commit (dirty checking). */
    @Transactional
    public PlayerResponse setBlocked(long playerId, boolean blocked) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
        player.setBlocked(blocked);
        return PlayerResponse.from(player);
    }
}
