package ir.sharif.aichallenge.server.thefinalbattle.model.graphic.message;

import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.RespawnedHero;
import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.StatusHero;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class StatusMessage {
    private List<StatusHero> heroes;
    private List<RespawnedHero> respawnedHeroes;
    private int[] scores;
}
