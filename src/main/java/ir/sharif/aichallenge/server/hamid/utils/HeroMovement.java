package ir.sharif.aichallenge.server.hamid.utils;

import ir.sharif.aichallenge.server.hamid.model.Cell;
import ir.sharif.aichallenge.server.hamid.model.Hero;
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
