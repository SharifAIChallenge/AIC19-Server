package ir.sharif.aichallenge.server.thefinalbattle.model.graphic;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatusHero {
    private int id;
    private int currentHP;
    private int remRespawnTime;
    private List<RemainingCooldown> remainingCooldowns;

    public StatusHero(int id, int currentHP, int remRespawnTime) {
        this.id = id;
        this.currentHP = currentHP;
        this.remRespawnTime = remRespawnTime;
    }
}
