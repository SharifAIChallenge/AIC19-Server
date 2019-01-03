package ir.sharif.aichallenge.TowerDefence;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ir.sharif.aichallenge.TowerDefence.Factory.Constants;
import ir.sharif.aichallenge.TowerDefence.Factory.TowerFactory;
import ir.sharif.aichallenge.TowerDefence.Factory.UnitFactory;
import ir.sharif.aichallenge.TowerDefence.GameObject.ArcherTower;
import ir.sharif.aichallenge.TowerDefence.GameObject.Tower;
import ir.sharif.aichallenge.TowerDefence.GameObject.Unit;
import ir.sharif.aichallenge.TowerDefence.Map.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by msi1 on 1/21/2018.
 */
public class Player
{
    private TowerFactory towerFactory = new TowerFactory();
    private UnitFactory unitFactory = new UnitFactory();

    private ArrayList<Tower> towers = new ArrayList<>();
    private HashMap<Integer, Tower> towersMap = new HashMap<>();

    private ArrayList<Unit> units = new ArrayList<>();

    private TurnEvents turnEvents;

    private HashMap<Integer, Tower> vision = new HashMap<>();

    private Player opponent;
    private int health = Constants.INIT_HEALTH;
    private int income;
    private int money = Constants.INIT_MONEY;
    private int turnover;
    private int id;
    private int nukeNum = Constants.NUMBER_OF_NUKES;
    private int beanNum = Constants.NUMBER_OF_BEANS;

    private Gson gson = new Gson();

    public Player(int id, TurnEvents turnEvents)
    {
        this.id = id;
        this.turnEvents = turnEvents;
        this.health = Constants.INIT_HEALTH;
        this.money = Constants.INIT_MONEY;
        this.turnover = Constants.INIT_MONEY;
        this.nukeNum = Constants.NUMBER_OF_NUKES;
        this.beanNum = Constants.NUMBER_OF_BEANS;
        this.income = 20;
    }

    public void sendUnit(ArrayList<Pair<Character, Integer>> allUnitData, Map map)
    {
        HashMap<Integer, Path> pathMap = map.getPathsMap();

        for (Pair<Character, Integer> unitData : allUnitData)
        {
            Path path = pathMap.get(unitData.getValue());
            if (path == null)
            {
                continue;
            }

            Unit unit = unitFactory.createUnit(unitData.getKey(), path, money);
            RoadCell cell = path.getCells().get(0);
            if (unit == null)
            {
                continue;
            }

            cell.addUnit(unit);
            units.add(unit);
            money = unitFactory.getLeftoverMoney();
            income += unitFactory.getCreationIncome();
            turnEvents.addNewUnit(this.id, unit);
        }
    }

    public void createTower(ArrayList<Pair<Character, int[]>> allTowerData, Map map)
    {
        for (Pair<Character, int[]> towerData : allTowerData)
        {
//            System.out.println("in createTower()");
            int[] locationAndLv = towerData.getValue();
            if (!map.isConstructableGrass(locationAndLv[0], locationAndLv[1]))
            {
//                System.out.println("Tower invalid!");
                continue;
            }
            Tower tower = towerFactory.createTower(towerData.getKey(), locationAndLv[0], locationAndLv[1], money);
            if (tower == null)
            {
                continue;
            }
            money = towerFactory.getLeftoverMoney();
            for (int i = 0; i < locationAndLv[2] - 1; i++)
            {
                upgradeTower(tower, true);
            }
//            System.out.println("Tower Valid!");
            towers.add(tower);
            towersMap.put(tower.getId(), tower);
            map.addTower(tower);

            turnEvents.addNewTower(this.id, tower); // TODO check this
            if (opponent.isInVision(tower))
            {
                turnEvents.addNewPartialTower(this.id, tower);
            }
        }
    }

    private boolean isInVision(Tower tower)
    {
        for (Unit unit : units)
        {
            if (unit.isInVision(tower))
            {
                return true;
            }
        }

        return false;
    }

    public void nuke(ArrayList<int[]> nukeLocations, Map map)
    {
        for (int[] nukeLocation : nukeLocations)
        {
            if (nukeNum == 0)
                return;
            Set<Unit> casualties = map.nuke(nukeLocation[0], nukeLocation[1]);
            if (casualties == null)
            {
                continue;
            }
            nukeNum--;
            this.getRewards(casualties);
            opponent.killUnits(casualties);
            turnEvents.addNewNukeLocation(this.id, nukeLocation);
        }
    }

    public void killUnits(Collection<Unit> casualties)
    {
        units.removeAll(casualties);
        turnEvents.addUnitCasualties(this.id, casualties);

        for (Unit unit : casualties)
        {
            unit.die();
        }
    }

    public void plantBean(ArrayList<int[]> plantLocations, Map map)
    {
        for (int[] plantLocation : plantLocations)
        {
            if (beanNum == 0)
                return;

            Cell theChosenCell = map.getCell(plantLocation[0], plantLocation[1]);
            if (theChosenCell instanceof GrassCell && (((GrassCell) theChosenCell).getTower() != null ||
                    ((GrassCell) theChosenCell).isConstructable()))
            {
                Tower tower = ((GrassCell) theChosenCell).plantBean();
                if (tower != null)
                {
                    opponent.destroyTower(tower);
                    deleteFromVision(tower);
                }
                beanNum--;
                turnEvents.addNewBeanLocation(this.id, plantLocation);
            }
        }
    }

    private void destroyTower(Tower tower)
    {
        towers.remove(tower);
        towersMap.remove(tower.getId());
        turnEvents.addTowerCasualties(this.id, tower);
//        deleteFromVision(tower);
    }

    private void deleteFromVision(Tower tower)
    {
        for (Unit unit : units)
        {
            unit.deleteFromVision(tower);
        }

        vision.remove(tower.getId());
    }

    public void upgradeTowers(ArrayList<Integer> towerIds)
    {
        for (Integer towerId : towerIds)
        {
            Tower tower = towersMap.get(towerId);
            if (tower == null)
                continue;

            upgradeTower(tower, false);
        }
    }

    private void upgradeTower(Tower tower, boolean isNewTower)
    {
        int moneyNeeded;
        if (tower instanceof ArcherTower)
        {
            moneyNeeded = (int) (Constants.TOWERS_CONSTANTS[0][1] *
                    Math.pow(Constants.TOWERS_CONSTANTS[0][2], tower.getLevel() - 1));
        } else
        {
            moneyNeeded = (int) (Constants.TOWERS_CONSTANTS[1][1] *
                    Math.pow(Constants.TOWERS_CONSTANTS[1][2], tower.getLevel() - 1));
        }

        if (moneyNeeded > money)
            return;

        money -= moneyNeeded;
        tower.setLevel(tower.getLevel() + 1);

        if (!isNewTower)
        {
            turnEvents.addNewTowerUpdate(this.id, tower);
            if (opponent.isInVision(tower))
            {
                turnEvents.addNewPartialTowerUpdate(this.id, tower);
            }
        }
    }

    public JsonElement getJsonUnits()
    {
        Object[][] unitsData = new Object[units.size()][8];
        JsonArray jsonArray;

        for (int i = 0; i < unitsData.length; i++)
        {
            Unit unit = units.get(i);
            unitsData[i] = unit.getData();
        }

        jsonArray = (JsonArray) gson.toJsonTree(unitsData);
        return jsonArray;
    }

    public JsonElement getOpponentJsonUnits()
    {
        ArrayList<Unit> enemyUnits = opponent.getUnits();
        Object[][] enemyUnitsData = new Object[enemyUnits.size()][5];
        JsonArray jsonArray;

        for (int i = 0; i < enemyUnits.size(); i++)
        {
            Unit unit = enemyUnits.get(i);
            enemyUnitsData[i] = unit.getPublicData();
        }

        jsonArray = (JsonArray) gson.toJsonTree(enemyUnitsData);
        return jsonArray;
    }

    public JsonElement getJsonTowers(ArrayList<Tower> towers, boolean isMyTower)
    {
        Object[][] towersData = new Object[towers.size()][5];
        JsonArray jsonArray;

        for (int i = 0; i < towers.size(); i++)
        {
            Tower tower = towers.get(i);
            if (isMyTower)
            {
                towersData[i] = tower.getData();
            } else
            {
                towersData[i] = tower.getDataForOpponent();
            }
        }

        jsonArray = (JsonArray) gson.toJsonTree(towersData);
        return jsonArray;
    }

    public JsonElement getOpponentJsonTowers()
    {
        ArrayList<Tower> enemyTowersInRange = new ArrayList<>();
        enemyTowersInRange.addAll(getPartialTowers());


        return getJsonTowers(enemyTowersInRange, false);
    }

    public JsonElement getSelfAndOppData()
    {
        JsonArray jsonArray;
        Object[] myData;
        Object[] oppData = new Object[3];
        Object[][] data;

        myData = getSelfData();

        oppData[0] = opponent.getHealth();
        oppData[1] = opponent.getBeanNum();
        oppData[2] = opponent.getNukeNum();

        data = new Object[][]{myData, oppData};
        jsonArray = (JsonArray) gson.toJsonTree(data);
        return jsonArray;
    }

    public Object[] getSelfData()
    {
        Object[] data = new Object[7];

        data[0] = health;
        data[1] = money;
        data[2] = income;
        data[3] = beanNum;
        data[4] = nukeNum;
        data[5] = towerFactory.getArcherTowerCost();
        data[6] = towerFactory.getCannonTowerCost();

        return data;
    }


    public JsonElement getEventsJsonDataForUI()
    {
        JsonObject result = new JsonObject();

        result.add("newunits", turnEvents.getNewUnitsJson());
        result.add("updtowers", turnEvents.getUpdTowersJson());
        result.add("updtowers_partial", turnEvents.getUpdTowersPartialJson());
        result.add("deadunits", turnEvents.getDeadUnitsJson(id, true));
        result.add("endofpath", turnEvents.getSuccessfulUnitsJson(id, true));
        result.add("destroyedtowers", turnEvents.getTowerCasualtiesJson(id, true));
        result.add("beans", turnEvents.getBeansJson(id, true));
        result.add("storms", turnEvents.getNukesJson(id, true));
        result.add("archer", turnEvents.getArcherAttacksJson());
        result.add("cannon", turnEvents.getCannonAttacksJson());


        return result;
    }

    public JsonElement getEventsJsonDataForClient()
    {
        JsonObject result = new JsonObject();

        result.add("deadunits", turnEvents.getDeadUnitsJson(id, false));
        result.add("endofpath", turnEvents.getSuccessfulUnitsJson(id, false));
        result.add("destroyedtowers", turnEvents.getTowerCasualtiesJson(id, false));
        result.add("beans", turnEvents.getBeansJson(id, false));
        result.add("storms", turnEvents.getNukesJson(id, false));

        return result;
    }

    public void upgradeVision(ArrayList<Tower> opponentTowers)
    {
        for (Unit unit : units)
        {
            unit.updateVision(opponentTowers);
            mergeVision(unit.getTowersInVision(), vision);
        }
    }

    private void mergeVision(HashMap<Integer, Tower> unitVision, HashMap<Integer, Tower> playerVision)
    {
        for (Integer towerId : unitVision.keySet())
        {
            Tower unitTower = unitVision.get(towerId);

            if (playerVision.containsKey(towerId))
            {
                Tower playerTower = playerVision.get(towerId);

                if (unitTower.getLevel() > playerTower.getLevel())
                {
                    playerTower.setLevel(unitTower.getLevel());
                }
            } else
            {
                Tower cloneTower = TowerFactory.createCopy(unitTower);
                playerVision.put(towerId, cloneTower);
            }
        }
    }

    public Collection<Tower> getPartialTowers()
    {
        return vision.values(); // TODO MUST CHECK THIS
    }

    public void getRewards(Collection<Unit> casualties)
    {
        for (Unit unit : casualties)
        {
            money += unit.getKillReward();
            turnover += unit.getKillReward();
        }
    }

    public void addIncome()
    {
        money += income;
        turnover += income;
    }

    public void clearTemporaryData()
    {
        turnEvents.clear();
    }

    public ArrayList<Tower> getTowers()
    {
        return towers;
    }

    public void setTowers(ArrayList<Tower> towers)
    {
        this.towers = towers;
    }

    public ArrayList<Unit> getUnits()
    {
        return units;
    }

    public void setUnits(ArrayList<Unit> units)
    {
        this.units = units;
    }

    public int getMoney()
    {
        return money;
    }

    public void setMoney(int money)
    {
        this.money = money;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getHealth()
    {
        return health;
    }

    public void setHealth(int health)
    {
        this.health = health;
    }

    public Player getOpponent()
    {
        return opponent;
    }

    public void setOpponent(Player opponent)
    {
        this.opponent = opponent;
    }

    public int getNukeNum()
    {
        return nukeNum;
    }

    public void setNukeNum(int nukeNum)
    {
        this.nukeNum = nukeNum;
    }

    public int getBeanNum()
    {
        return beanNum;
    }

    public void setBeanNum(int beanNum)
    {
        this.beanNum = beanNum;
    }

    public int getIncome()
    {
        return income;
    }

    public void setIncome(int income)
    {
        this.income = income;
    }

    public int getTurnover()
    {
        return turnover;
    }
}
