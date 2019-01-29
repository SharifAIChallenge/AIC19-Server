package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CastedAbility {
    private Hero casterHero;
    private List<Hero> targetHeroes;
    private Cell startCell;
    private Cell endCell;
    private Ability ability;
}
