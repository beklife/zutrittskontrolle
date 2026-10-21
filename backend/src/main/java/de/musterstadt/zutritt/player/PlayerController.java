package de.musterstadt.zutritt.player;

import de.musterstadt.zutritt.player.dto.BlockPlayerRequest;
import de.musterstadt.zutritt.player.dto.CreatePlayerRequest;
import de.musterstadt.zutritt.player.dto.PlayerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<PlayerResponse> findAll() {
        return playerService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse create(@Valid @RequestBody CreatePlayerRequest request) {
        return playerService.create(request);
    }

    @PutMapping("/{id}/block")
    public PlayerResponse setBlocked(@PathVariable long id, @Valid @RequestBody BlockPlayerRequest request) {
        return playerService.setBlocked(id, request.blocked());
    }
}
