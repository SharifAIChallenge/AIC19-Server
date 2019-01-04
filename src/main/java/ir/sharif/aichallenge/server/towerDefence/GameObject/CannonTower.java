package ir.sharif.aichallenge.server.towerDefence.GameObject;


import ir.sharif.aichallenge.server.towerDefence.Factory.Constants;
import ir.sharif.aichallenge.server.towerDefence.Map.Path;
import ir.sharif.aichallenge.server.towerDefence.Map.RoadCell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by msi1 on 1/21/2018.
 */
public class CannonTower extends Tower
{
    public CannonTower(int id, int x, int y, int lv, int cost)
    {
        super(id, x, y, lv, (int) Constants.TOWERS_CONSTANTS[1][3], Constants.TOWERS_CONSTANTS[1][4],
                (int) Constants.TOWERS_CONSTANTS[1][5], (int) Constants.TOWERS_CONSTANTS[1][6], cost);
    }

    @Override
    public ArrayList<Unit> fire(Unit unit)
    {
        ArrayList<Unit> casualties = new ArrayList<>();
        if (tickPerAttack == attackCounter)
        {
            RoadCell cell = unit.getPath().getCells().get(unit.getCurrentCellIndex());
            for (Unit target : cell.getUnits())
            {
                target.setHealth(target.getHealth() - (int) (getDamage()*Math.pow(getDamageCoeff(), getLevel() - 1)));
                if (target.getHealth() <= 0)
                {
                    casualties.add(target);
                }
            }
        }

        return casualties;
    }

    @Override
    protected ArrayList<Unit> secondTargetFinder(ArrayList<Unit> targets)
    {
        RoadCell targetCell;
        ArrayList<Unit> finalTargets = new ArrayList<>();
        HashMap<RoadCell, Unit> unitCellMap = createCellMap(targets);
        Collection<RoadCell> cellSet = unitCellMap.keySet();
        cellSet = filterOnUnitNum(cellSet);
        if (cellSet.size() > 1)
        {
            cellSet = filterOnHealth(cellSet);
        }

        if (cellSet.size() > 1)
        {
            targetCell = findFinalTarget(unitCellMap, cellSet);
        } else
        {
            targetCell = cellSet.iterator().next();
        }

        finalTargets.addAll(targetCell.getUnits());

        return finalTargets;
    }

    private RoadCell findFinalTarget(HashMap<RoadCell, Unit> unitCellMap, Collection<RoadCell> cellSet)
    {
        RoadCell targetCell = null;
        int minHealth = Integer.MAX_VALUE;

        for (RoadCell cell : cellSet)
        {
            Unit unit = unitCellMap.get(cell);
            if (unit.getHealth() < minHealth)
            {
                targetCell = cell;
                minHealth = unit.getHealth();
            }
        }

        return targetCell;
    }

    private Collection<RoadCell> filterOnHealth(Collection<RoadCell> cellSet)
    {
        Collection<RoadCell> resultCells = new ArrayList<>();
        int minHealth = Integer.MAX_VALUE;

        for (RoadCell cell : cellSet)
        {
            int completeHealth = cell.getCompleteHealth();

            if (completeHealth < minHealth)
            {
                resultCells.clear();
                resultCells.add(cell);
                minHealth = completeHealth;
            } else if (completeHealth == minHealth)
            {
                resultCells.add(cell);
            }
        }

        return resultCells;
    }

    private Collection<RoadCell> filterOnUnitNum(Collection<RoadCell> cellSet)
    {
        Collection<RoadCell> resultCells = new ArrayList<>();
        int minNum = Integer.MAX_VALUE;

        for (RoadCell cell : cellSet)
        {
            if (cell.getUnits().size() < minNum)
            {
                resultCells.clear();
                resultCells.add(cell);
                minNum = cell.getUnits().size();
            } else if (cell.getUnits().size() == minNum)
            {
                resultCells.add(cell);
            }
        }

        return resultCells;
    }

    private HashMap<RoadCell, Unit> createCellMap(ArrayList<Unit> units)
    {
        HashMap<RoadCell, Unit> cellMap = new HashMap<>();

        for (Unit unit : units)
        {
            Path unitPath = unit.getPath();
            int unitPathIndex = unit.getCurrentCellIndex();
            RoadCell cell = unitPath.getCells().get(unitPathIndex);
            if (!(cellMap.containsKey(cell) && cellMap.get(cell).getHealth() < unit.getHealth()))
            {
                cellMap.put(cell, unit);
            }
        }

        return cellMap;
    }
}
