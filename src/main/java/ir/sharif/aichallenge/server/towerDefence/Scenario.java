package ir.sharif.aichallenge.server.towerDefence;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import ir.sharif.aichallenge.server.towerDefence.GameObject.ArcherTower;
import ir.sharif.aichallenge.server.towerDefence.GameObject.CannonTower;
import ir.sharif.aichallenge.server.towerDefence.GameObject.Tower;
import ir.sharif.aichallenge.server.towerDefence.GameObject.Unit;
import ir.sharif.aichallenge.server.towerDefence.Map.Map;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by msi1 on 1/21/2018.
 */
public class Scenario
{
    private Player attacker;
    private Player defender;
    private Map map;
    private int turn;
    private Gson gson = new Gson();
    private TurnEvents turnEvents;

    public Scenario(Player defender, Player attacker, Map map, TurnEvents turnEvents)
    {
        this.defender = defender;
        this.attacker = attacker;
        this.map = map;
        this.turnEvents = turnEvents;
    }

    public void triggerTowers()
    {
        ArrayList<Tower> towers = defender.getTowers();
        HashSet<Unit> casualties = new HashSet<>();
        for (Tower tower : towers)
        {
            Unit unit = tower.locateTarget(map);
            if (unit == null)
            {
                tower.tick(false);
                continue;
            }
            casualties.addAll(tower.fire(unit));
            if (tower instanceof ArcherTower)
            {
                turnEvents.addArcherAttack(tower, unit);
            } else if (tower instanceof CannonTower)
            {
                turnEvents.addCannonAttack(tower, unit);
            }
            tower.tick(true);
        }
        defender.getRewards(casualties);
        attacker.killUnits(casualties);
    }

    public void moveUnits()
    {
        ArrayList<Unit> unitsAtEndOfPath = new ArrayList<>();
        ArrayList<Unit> units = attacker.getUnits();
        for (Unit unit : units)
        {
            unit.move();
            if (unit.isAtGoal())
            {
                unit.die();
                unitsAtEndOfPath.add(unit);
                turnEvents.addSuccessfulUnit(defender.getId(), unit);
                defender.setHealth(defender.getHealth() - unit.getDamage());
                if (defender.getHealth() < 0 )
                {
                    defender.setHealth(0);
                }
            }
        }

        units.removeAll(unitsAtEndOfPath);
    }

    public void createUnits(ArrayList<Pair<Character, Integer>> allUnitData)
    {
        attacker.sendUnit(allUnitData, map);
    }

    public void createTowers(ArrayList<Pair<Character, int[]>> allTowerData)
    {
        defender.createTower(allTowerData, map);
    }

    public void upgradeTowers(ArrayList<Integer> towerIds)
    {
        defender.upgradeTowers(towerIds);
    }

    public void nuke(ArrayList<int[]> nukeLocations)
    {
        defender.nuke(nukeLocations, map);
    }

    public void plantBean(ArrayList<int[]> beanLocations)
    {
        attacker.plantBean(beanLocations, map);
    }

    public JsonElement getUnitJsonForUI()
    {
        ArrayList<Unit> playerOneUnits = defender.getUnits();
        ArrayList<Unit> playerTwoUnits = attacker.getUnits();
        Object[][] data = new Object[playerOneUnits.size() + playerTwoUnits.size()][8];

        for (int i = 0; i < playerOneUnits.size(); i++)
        {
            data[i] = new Object[9];
            Object[] unitData = playerOneUnits.get(i).getData();
            data[i][0] = 1 - defender.getId();
            mergeArray(data[i], unitData, 1);
//            data[i] = new Object[]{1 - defender.getId(), playerOneUnits.get(i).getData()};
        }

        for (int i = 0; i < playerTwoUnits.size(); i++)
        {
            data[i + playerOneUnits.size()] = new Object[9];
            data[i + playerOneUnits.size()][0] = 1 - attacker.getId();
            mergeArray(data[i + playerOneUnits.size()], playerTwoUnits.get(i).getData(), 1);
//            data[i + playerOneUnits.size()] = new Object[]{1 - attacker.getId(), playerTwoUnits.get(i).getData()};
        }

        return gson.toJsonTree(data);
    }

    private void mergeArray(Object[] firstArray, Object[] secondArray, int startIndex)
    {
        for (int i = 0; i < secondArray.length; i++)
        {
            firstArray[i + startIndex] = secondArray[i];
        }
    }

    public JsonElement getTowerJsonForUI()
    {
        ArrayList<Tower> playerOneTowers = defender.getTowers();
        ArrayList<Tower> playerTwoTowers = attacker.getTowers();
        Object[][] data = new Object[playerOneTowers.size() + playerTwoTowers.size()][6];

        for (int i = 0; i < playerOneTowers.size(); i++)
        {
            data[i] = new Object[6];
            Object[] unitData = playerOneTowers.get(i).getData();
            data[i][0] = defender.getId();
            mergeArray(data[i], unitData, 1);
        }
        for (int i = 0; i < playerTwoTowers.size(); i++)
        {
            data[i + playerOneTowers.size()] = new Object[6];
            data[i + playerOneTowers.size()][0] = attacker.getId();
            mergeArray(data[i + playerOneTowers.size()], playerTwoTowers.get(i).getData(), 1);
        }

        return gson.toJsonTree(data);
    }

    public JsonElement getTowerPartialJsonForUI()
    {
        ArrayList<Tower> playerOnePartials = new ArrayList<>();
        ArrayList<Tower> playerTwoPartials = new ArrayList<>();

        playerOnePartials.addAll(attacker.getPartialTowers());
        playerTwoPartials.addAll(defender.getPartialTowers());

        Object[][] data = new Object[playerOnePartials.size() + playerTwoPartials.size()][4];

        for (int i = 0; i < playerOnePartials.size(); i++)
        {
            data[i] = playerOnePartials.get(i).getData();
        }

        for (int i = 0; i < playerTwoPartials.size(); i++)
        {
            data[i + playerOnePartials.size()] = playerTwoPartials.get(i).getData();
        }

        return gson.toJsonTree(data);
    }

    public JsonElement getPlayerJsonForUI()
    {
        Object[][] data = new Object[2][5];
        data[0] = defender.getSelfData();
        data[1] = attacker.getSelfData();

        return gson.toJsonTree(data);
    }

    public JsonElement getEventJsonForUI()
    {
        return defender.getEventsJsonDataForUI();
    }

    public void updateUnitsVision()
    {
        attacker.upgradeVision(defender.getTowers());
    }

    public Player getAttacker()
    {
        return attacker;
    }

    public void setAttacker(Player attacker)
    {
        this.attacker = attacker;
    }

    public Player getDefender()
    {
        return defender;
    }

    public void setDefender(Player defender)
    {
        this.defender = defender;
    }

    public Map getMap()
    {
        return map;
    }

    public void setMap(Map map)
    {
        this.map = map;
    }

    public int getTurn()
    {
        return turn;
    }

    public void setTurn(int turn)
    {
        this.turn = turn;
    }
}
