package ir.sharif.aichallenge.TowerDefence.Map;

import ir.sharif.aichallenge.TowerDefence.GameObject.Unit;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by msi1 on 1/21/2018.
 */
public class RoadCell extends Cell
{
    private int id;
    private ArrayList<Unit> units = new ArrayList<>();
    private ArrayList<Path> paths;
    private HashMap<Unit, Integer> unitMap = new HashMap<>();

    public RoadCell(int x, int y)
    {
        super(x, y);
    }

    public void removeUnit(Unit unit)
    {
//        int index = unitMap.get(unit);

        units.remove(unit);
        unitMap.remove(unit);
    }

    public void addUnit(Unit unit)
    {
        units.add(unit);
        unitMap.put(unit, units.size() - 1);
    }

    public ArrayList<Unit> destroy()
    {
        ArrayList<Unit> deadUnits = new ArrayList<>();
        deadUnits.addAll(units);
        units.clear();
        unitMap.clear();
        return deadUnits;
    }

    public int getCompleteHealth()
    {
        int completeHealth = 0;

        for (Unit unit : units)
        {
            completeHealth += unit.getHealth();
        }

        return completeHealth;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public ArrayList<Unit> getUnits()
    {
        return units;
    }

    public void setUnits(ArrayList<Unit> units)
    {
        this.units = units;
    }

    public ArrayList<Path> getPaths()
    {
        return paths;
    }

    public void setPaths(ArrayList<Path> paths)
    {
        this.paths = paths;
    }
}
