package ir.sharif.aichallenge.towerDefence.GameObject;


import ir.sharif.aichallenge.towerDefence.Map.Cell;
import ir.sharif.aichallenge.towerDefence.Map.Map;
import ir.sharif.aichallenge.towerDefence.Map.RoadCell;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by msi1 on 1/21/2018.
 */
public abstract class Tower extends WarObject
{
    private int x;
    private int y;
    private int damage;
    private double damageCoeff;
    private int cost;
    private int attackRange;
    protected int tickPerAttack;
    protected int attackCounter;
    private int level;

    public Tower(int id, int x, int y, int level, int damage, double damageCoeff,
                 int tickPerAttack, int attackRange, int cost)
    {
        super(id);
        this.x = x;
        this.y = y;
        this.level = level;
        this.damage = damage;
        this.damageCoeff = damageCoeff;
        this.tickPerAttack = tickPerAttack;
        this.attackCounter = tickPerAttack;
        this.attackRange = attackRange;
        this.cost = cost;
    }

    public Unit locateTarget(Map map)
    {
        ArrayList<Unit> targets = firstTargetFinder(map);
        if (targets.size() > 1)
        {
            targets = secondTargetFinder(targets);
        }

        if (targets.size() > 0)
        {
            return targets.get(0);
        }

        return null;
    }

    public void tick(boolean targetIsInRange)
    {
        if (!targetIsInRange && attackCounter == tickPerAttack)
        {
            return;
        }

        if (--attackCounter == 0)
        {
            attackCounter = tickPerAttack;
        }
    }

    public abstract ArrayList<Unit> fire(Unit unit);

    protected ArrayList<Unit> firstTargetFinder(Map map)
    {
        ArrayList<Unit> targets = new ArrayList<>();
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < map.getCells().length; i++)
        {
            for (int j = 0; j < map.getCells()[i].length; j++)
            {
                Cell cell = map.getCell(i, j);
                if (cell instanceof RoadCell && Math.abs(cell.getX() - x) + Math.abs(cell.getY() - y) <= 2)
                {
                    ArrayList<Unit> nearestTargets = findNearestUnitToEnd((RoadCell) cell);
                    if (nearestTargets.size() == 0)
                    {
                        continue;
                    }
                    int distance = nearestTargets.get(nearestTargets.size() - 1).getDistance();
                    if (distance < minDistance)
                    {
                        targets.clear();
                        targets.addAll(nearestTargets);
                        minDistance = distance;
                    } else if (distance == minDistance)
                    {
                        targets.addAll(nearestTargets);
                    }
                }
            }
        }

        return targets;
    }

    protected ArrayList<Unit> findNearestUnitToEnd(RoadCell cell)
    {
        ArrayList<Unit> targets = new ArrayList<Unit>();
        ArrayList<Unit> units = cell.getUnits();
        int minDistance = Integer.MAX_VALUE;

        for (Unit unit : units)
        {
            if (unit.getDistance() < minDistance)
            {
                targets.clear();
                targets.add(unit);
                minDistance = unit.getDistance();
            } else if (unit.getDistance() == minDistance)
            {
                targets.add(unit);
            }
        }

        return targets;
    }

    protected abstract ArrayList<Unit> secondTargetFinder(ArrayList<Unit> targets);

    public Object[] getData()
    {
        Object[] data = new Object[5];

        data[0] = id;
        data[1] = (this instanceof ArcherTower) ? 'a' : 'c';
        data[2] = level;
        data[3] = new Point(x, y);
        data[4] = cost;

        return data;
    }

    public Object[] getDataForOpponent()
    {
        Object[] data = new Object[4];

        data[0] = id;
        data[1] = (this instanceof ArcherTower) ? 'a' : 'c';
        data[2] = level;
        data[3] = new Point(x, y);

        return data;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getDamage()
    {
        return damage;
    }

    public void setDamage(int damage)
    {
        this.damage = damage;
    }

    public int getCost()
    {
        return cost;
    }

    public void setCost(int cost)
    {
        this.cost = cost;
    }

    public int getAttackRange()
    {
        return attackRange;
    }

    public void setAttackRange(int attackRange)
    {
        this.attackRange = attackRange;
    }

    public int getTickPerAttack()
    {
        return tickPerAttack;
    }

    public void setTickPerAttack(int tickPerAttack)
    {
        this.tickPerAttack = tickPerAttack;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public double getDamageCoeff()
    {
        return damageCoeff;
    }

    public int getAttackCounter()
    {
        return attackCounter;
    }

    public void setAttackCounter(int attackCounter)
    {
        this.attackCounter = attackCounter;
    }
}
