package ir.sharif.aichallenge.server.hamid.utils;

import ir.sharif.aichallenge.server.hamid.model.Cell;
import ir.sharif.aichallenge.server.hamid.model.Hero;
import ir.sharif.aichallenge.server.hamid.model.Map;
import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.enums.AbilityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbilityTools {
    private Map map;
    private VisionTools visionTools;
    private List<Hero> myHeroes;
    private List<Hero> oppHeroes;

    public AbilityTools(Map map, List<Hero> myHeroes, List<Hero> oppHeroes) {
        this.map = map;
        visionTools = new VisionTools(map);
        this.myHeroes = myHeroes;
        this.oppHeroes = oppHeroes;
    }

    public Hero[] getAbilityTargets(Ability ability, Cell startCell, Cell targetCell)
    {
        Cell[] impactCells = getImpactCells(ability, startCell, targetCell);
        Set<Cell> affectedCells = new HashSet<>();
        for (Cell cell : impactCells)
        {
            affectedCells.addAll(getCellsInAOE(cell, ability.getAreaOfEffect()));
        }
        if (ability.getType() == AbilityType.HEAL)
        {
            return getMyHeroesInCells(affectedCells.toArray(new Cell[0]));
        } else
        {
            return getOppHeroesInCells(affectedCells.toArray(new Cell[0]));
        }
    }

    public Cell[] getImpactCells(Ability ability, Cell startCell, Cell targetCell)
    {
        if (ability.isLobbing())
        {
            return new Cell[]{targetCell};
        }
        if (startCell.isWall() || startCell == targetCell)
        {
            return new Cell[]{startCell};
        }
        ArrayList<Cell> impactCells = new ArrayList<>();
        Cell[] rayCells = visionTools.getRayCells(startCell, targetCell);
        Cell lastCell = null;
        for (Cell cell : rayCells)
        {
            if (visionTools.manhattanDistance(startCell, cell) > ability.getRange())
                break;
            lastCell = cell;
            if ((getOppHero(cell) != null && !ability.getType().equals(AbilityType.HEAL))
                    || ((getMyHero(cell) != null && ability.getType().equals(AbilityType.HEAL))))
            {
                impactCells.add(cell);
                if (!ability.isPiercing()) break;
            }
        }
        if (!impactCells.contains(lastCell))
            impactCells.add(lastCell);
        return impactCells.toArray(new Cell[0]);
    }

    public Hero getOppHero(Cell cell)
    {
        for (Hero hero : oppHeroes)
        {
            if (hero.getCell() == cell)
                return hero;
        }
        return null;
    }

    public Hero getOppHero(int cellRow, int cellColumn)
    {
        if (!map.isInMap(cellRow, cellColumn)) return null;
        return getOppHero(map.getCell(cellRow, cellColumn));
    }

    public Hero getMyHero(Cell cell)
    {
        for (Hero hero : myHeroes)
        {
            if (hero.getCell() == cell)
                return hero;
        }
        return null;
    }

    private ArrayList<Cell> getCellsInAOE(Cell impactCell, int AOE)
    {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int row = impactCell.getRow() - AOE; row <= impactCell.getRow() + AOE; row++)
        {
            for (int col = impactCell.getColumn() - AOE; col <= impactCell.getColumn() + AOE; col++)
            {
                if (!map.isInMap(row, col)) continue;
                Cell cell = map.getCell(row, col);
                if (visionTools.manhattanDistance(impactCell, cell) <= AOE)
                    cells.add(cell);
            }
        }
        return cells;
    }

    private Hero[] getOppHeroesInCells(Cell[] cells)
    {
        ArrayList<Hero> heroes = new ArrayList<>();
        for (Cell cell : cells)
        {
            Hero hero = getOppHero(cell);
            if (hero != null)
            {
                heroes.add(hero);
            }
        }
        return heroes.toArray(new Hero[0]);
    }

    private Hero[] getMyHeroesInCells(Cell[] cells)
    {
        ArrayList<Hero> heroes = new ArrayList<>();
        for (Cell cell : cells)
        {
            Hero hero = getMyHero(cell);
            if (hero != null)
            {
                heroes.add(hero);
            }
        }
        return heroes.toArray(new Hero[0]);
    }
}
