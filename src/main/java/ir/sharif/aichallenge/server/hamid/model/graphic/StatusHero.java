package ir.sharif.aichallenge.server.hamid.model.graphic;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatusHero {
    private int id;
    private int currentHP;
    private List<RemainingCooldown> remainingCooldowns;
}
