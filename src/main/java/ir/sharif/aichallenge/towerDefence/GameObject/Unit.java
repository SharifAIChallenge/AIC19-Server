package ir.sharif.aichallenge.towerDefence.GameObject;

import ir.sharif.aichallenge.towerDefence.Factory.TowerFactory;
import ir.sharif.aichallenge.towerDefence.Map.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by msi1 on 1/21/2018.
 */
public class Unit extends WarObject
{
    private int level;
    private int damage;
    private int health;
    private int maxHealth;
    private int tickPerTile;
    private int moveCounter;
    private int visionRange;
    private int killReward;
    private Path path;
    private int currentCellIndex = 0;
    private boolean isAtGoal = false;
    private HashMap<Integer, Tower> towersInVision = new HashMap<Integer, Tower>();

    public Unit(int id, int level, int damage, int health, int tickPerTile, int visionRange, int killReward, Path path)
    {
        super(id);
        this.level = level;
        this.damage = damage;
        this.health = health;
        this.maxHealth = health;
        this.tickPerTile = tickPerTile;
        this.moveCounter = tickPerTile;
        this.visionRange = visionRange;
        this.killReward = killReward;
        this.path = path;

    }

    public void move()
    {
        moveCounter--;
        if (moveCounter == 0)
        {
            ArrayList<RoadCell> pathCells = path.getCells();

            RoadCell currentRoad = pathCells.get(currentCellIndex);
            currentRoad.removeUnit(this);

            if (currentCellIndex == pathCells.size() - 1)
            {
                isAtGoal = true;
                moveCounter = tickPerTile;
                return;
            }

            RoadCell nextCell = pathCells.get(currentCellIndex + 1);
            nextCell.addUnit(this);

            currentCellIndex++;
            moveCounter = tickPerTile;
        }
    }

    public Object[] getData()
    {
        Object[] data = new Object[8];

        data[0] = id;
        data[1] = (this instanceof LightUnit) ? 'l' : 'h';
        data[2] = level;
        data[3] = health;
        data[4] = new Point(path.getCells().get(currentCellIndex).getX(), path.getCells().get(currentCellIndex).getY());
        data[5] = moveCounter;
        data[6] = path.getId();
        data[7] = maxHealth;

        return data;
    }

    public Object[] getPublicData()
    {
        Object[] data = new Object[4];

        data[0] = id;
        data[1] = (this instanceof LightUnit) ? 'l' : 'h';
        data[2] = level;
        data[3] = new Point(path.getCells().get(currentCellIndex).getX(), path.getCells().get(currentCellIndex).getY());

        return data;
    }

    public ArrayList<Tower> findTowers(Map map)
    {
        ArrayList<Tower> towersInRange = new ArrayList<Tower>();

        for (int i = 0; i < map.getWidth(); i++)
        {
            for (int j = 0; j < map.getHeight(); j++)
            {
                Cell cell = map.getCell(i, j);

                if (cell instanceof GrassCell && ((GrassCell) cell).getTower() != null)
                {
                    towersInRange.add(((GrassCell) cell).getTower());
                }
            }
        }

        return towersInRange;
    }

    public boolean isInVision(Tower tower)
    {
        return Math.abs(tower.getX() - this.getPath().getCells().get(currentCellIndex).getX()) +
                Math.abs(tower.getY() - this.getPath().getCells().get(currentCellIndex).getY()) <= visionRange;
    }

    public void updateVision(ArrayList<Tower> opponentTowers)
    {
        for (Tower tower : opponentTowers)
        {
            if (isInVision(tower))
            {
                if (towersInVision.containsKey(tower.getId()))
                {
                    Tower inVisionTower = towersInVision.get(tower.getId());

                    if (!TowerFactory.isSameType(tower, inVisionTower))
                    {
                        Tower cloneTower = TowerFactory.createCopy(tower);
                        towersInVision.put(cloneTower.getId(), cloneTower);
                    } else if (inVisionTower.getLevel() != tower.getLevel())
                    {
                        inVisionTower.setLevel(tower.getLevel());
                    }

                } else
                {
                    Tower cloneTower = TowerFactory.createCopy(tower);
                    towersInVision.put(cloneTower.getId(), cloneTower);
                }
            }
        }
    }

    public void deleteFromVision(Tower tower)
    {
        towersInVision.remove(tower.getId());
    }

    public void die()
    {
        RoadCell currentCell = path.getCells().get(currentCellIndex);
        currentCell.getUnits().remove(this);
    }

    public int getDistance()
    {
        return path.getCells().size() - currentCellIndex;
    }

    public int getHealth()
    {
        return health;
    }

    public void setHealth(int health)
    {
        this.health = health;
    }

    public int getTickPerTile()
    {
        return tickPerTile;
    }

    public void setTickPerTile(int tickPerTile)
    {
        this.tickPerTile = tickPerTile;
    }

    public int getKillReward()
    {
        return killReward;
    }

    public void setKillReward(int killReward)
    {
        this.killReward = killReward;
    }

    public int getDamage()
    {
        return damage;
    }

    public void setDamage(int damage)
    {
        this.damage = damage;
    }

    public int getVisionRange()
    {
        return visionRange;
    }

    public void setVisionRange(int visionRange)
    {
        this.visionRange = visionRange;
    }

    public int getCurrentCellIndex()
    {
        return currentCellIndex;
    }

    public void setCurrentCellIndex(int currentCellIndex)
    {
        this.currentCellIndex = currentCellIndex;
    }

    public Path getPath()
    {
        return path;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public boolean isAtGoal()
    {
        return isAtGoal;
    }

    public HashMap<Integer, Tower> getTowersInVision()
    {
        return towersInVision;
    }

    public int getMaxHealth()
    {
        return maxHealth;
    }
}
