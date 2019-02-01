package ir.sharif.aichallenge.server.hamid.model.graphic.message;

import ir.sharif.aichallenge.server.hamid.model.graphic.RespawnedHero;
import ir.sharif.aichallenge.server.hamid.model.graphic.StatusHero;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class StatusMessage {
    List<StatusHero> heroes;
    List<RespawnedHero> respawnedHeroes;
}
