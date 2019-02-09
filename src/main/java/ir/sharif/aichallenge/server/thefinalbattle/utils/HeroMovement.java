package ir.sharif.aichallenge.server.thefinalbattle.utils;

import ir.sharif.aichallenge.server.thefinalbattle.model.Cell;
import ir.sharif.aichallenge.server.thefinalbattle.model.Hero;
import ir.sharif.aichallenge.server.thefinalbattle.model.ability.Ability;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeroMovement
{
    private Hero hero;
    private Cell startCell;
    private Cell endCell;
    private int priority;
    private int ap;
    private Ability ability;

    public HeroMovement(Cell startCell, Cell endCell, Hero hero)
    {
        this.hero = hero;
        this.startCell = startCell;
        this.endCell = endCell;
    }

    public int getNeededAP()
    {
        if (startCell == endCell)
            return 0;
        return hero.getMoveApCost();
    }
}
