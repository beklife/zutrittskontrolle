package de.musterstadt.zutritt.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findAllByOrderByLastNameAscFirstNameAsc();
}
