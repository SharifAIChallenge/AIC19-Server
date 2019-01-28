package ir.sharif.aichallenge.server.hamid.model.client;

import lombok.Setter;

@Setter
public class CastedAbility {
    private int casterId;
    private int targetHeroId;
    private EmptyCell startCell;
    private EmptyCell endCell;
    private String abilityName;
}
