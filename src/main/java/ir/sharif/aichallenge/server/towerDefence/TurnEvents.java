package ir.sharif.aichallenge.server.towerDefence;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import ir.sharif.aichallenge.server.towerDefence.GameObject.Tower;
import ir.sharif.aichallenge.server.towerDefence.GameObject.Unit;
import ir.sharif.aichallenge.server.towerDefence.GameObject.WarObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
/**
 * Created by msi1 on 1/31/2018.
 */
public class TurnEvents
{
    private ArrayList<Pair<Integer, Unit>> turnNewUnits = new ArrayList<>();
    private ArrayList<Pair<Integer, Tower>> turnNewTowers = new ArrayList<>();
    private ArrayList<Pair<Integer, Tower>> turnNewPartialTowers = new ArrayList<>();

    private ArrayList<Pair<Integer, Tower>> turnTowerUpdates = new ArrayList<>();
    private ArrayList<Pair<Integer, Tower>> turnPartialTowerUpdates = new ArrayList<>();

    private ArrayList<Pair<Integer, WarObject>> turnUnitCasualties = new ArrayList<>();
    private ArrayList<Pair<Integer, WarObject>> turnTowerCasualties = new ArrayList<>();

    private ArrayList<Pair<Integer, Point>> turnNukeLocations = new ArrayList<>();
    private ArrayList<Pair<Integer, Point>> turnBeanLocations = new ArrayList<>();

    private ArrayList<Pair<Tower, Unit>> turnArcherAttacks = new ArrayList<>();
    private ArrayList<Pair<Tower, Unit>> turnCannonAttacks = new ArrayList<>();

    private ArrayList<Pair<Integer, WarObject>> turnSuccessfulUnits = new ArrayList<>();

    private Gson gson = new Gson();
    private JsonArray jsonArray;

    public void addNewUnit(int playerId, Unit unit)
    {
        turnNewUnits.add(new Pair<>(1 - playerId, unit));
    }

    public void addNewTower(int playerId, Tower tower)
    {
        turnNewTowers.add(new Pair<>(playerId, tower));
    }

    public void addNewPartialTower(int playerId, Tower tower)
    {
        turnNewPartialTowers.add(new Pair<>(playerId, tower));
    }

    public void addNewNukeLocation(int playerId, int[] nukeLocation)
    {
        Point point = new Point(nukeLocation[0], nukeLocation[1]);
        turnNukeLocations.add(new Pair<>(playerId, point));
    }

    public void addNewBeanLocation(int playerId, int[] beanLocation)
    {
        Point point = new Point(beanLocation[0], beanLocation[1]);
        turnBeanLocations.add(new Pair<>(1 - playerId, point));
    }

    public void addUnitCasualties(int playerId, Collection<Unit> casualties)
    {
        for (Unit unit : casualties)
        {
            turnUnitCasualties.add(new Pair<>(1 - playerId, unit));
        }
    }

    public void addTowerCasualties(int playerId, Tower tower)
    {
        turnTowerCasualties.add(new Pair<>(playerId, tower));
    }

    public void addNewTowerUpdate(int playerId, Tower tower)
    {
        turnTowerUpdates.add(new Pair<>(playerId, tower));
    }

    public void addNewPartialTowerUpdate(int playerId, Tower tower)
    {
        turnPartialTowerUpdates.add(new Pair<>(playerId, tower));
    }

    public void addSuccessfulUnit(int mapId, Unit unit)
    {
        turnSuccessfulUnits.add(new Pair<>(mapId, unit));
    }

    public void addArcherAttack(Tower tower, Unit unit)
    {
        turnArcherAttacks.add(new Pair<>(tower, unit));
    }

    public void addCannonAttack(Tower tower, Unit unit)
    {
        turnCannonAttacks.add(new Pair<>(tower, unit));
    }

    public JsonArray getNewUnitsJson()
    {
        Object[][] unitsData = new Object[turnNewUnits.size()][8];
        for (int i = 0; i < turnNewUnits.size(); i++)
        {
            Unit unit = turnNewUnits.get(i).getValue();
            unitsData[i] = new Object[]{turnNewUnits.get(i).getKey(), unit.getData()};
        }

        jsonArray = (JsonArray) gson.toJsonTree(unitsData);
        return jsonArray;
    }

    public JsonArray getUpdTowersJson()
    {
        return getUpdTowers(turnNewTowers, turnTowerUpdates);
    }

    public JsonArray getUpdTowersPartialJson()
    {
        return getUpdTowers(turnNewPartialTowers, turnPartialTowerUpdates);
    }

    private JsonArray getUpdTowers(ArrayList<Pair<Integer, Tower>> newTowers, ArrayList<Pair<Integer, Tower>> updateTowers)
    {
        Object[][] towerData = new Object[updateTowers.size() + newTowers.size()][4];

        for (int i = 0; i < updateTowers.size(); i++)
        {
            Tower tower = updateTowers.get(i).getValue();
            towerData[i] = new Object[]{updateTowers.get(i).getKey(), tower.getId(), tower.getLevel(), false};
        }
        for (int i = 0; i < newTowers.size(); i++)
        {
            Tower tower = newTowers.get(i).getValue();
            towerData[i + updateTowers.size()] = new Object[]{newTowers.get(i).getKey(), tower.getId(), tower.getLevel(), true};
        }

        jsonArray = (JsonArray) gson.toJsonTree(towerData);
        return jsonArray;
    }

    public JsonArray getDeadUnitsJson(int id, boolean isForUI)
    {
        return getWarObjectJson(turnUnitCasualties, id, isForUI);
    }

    public JsonArray getSuccessfulUnitsJson(int id, boolean isForUI)
    {
        return getWarObjectJson(turnSuccessfulUnits, id, isForUI);
    }

    public JsonArray getTowerCasualtiesJson(int id, boolean isForUI)
    {
        return getWarObjectJson(turnTowerCasualties, id, isForUI);
    }

    private JsonArray getWarObjectJson(ArrayList<Pair<Integer, WarObject>> warObjects, int id, boolean isForUI)
    {
        Object[][] warObjectsData = new Object[warObjects.size()][2];
        for (int i = 0; i < warObjects.size(); i++)
        {
            WarObject warObject = warObjects.get(i).getValue();
            if (isForUI)
            {
                warObjectsData[i] = new Object[]{warObjects.get(i).getKey(), warObject.getId()};
            } else
            {
                warObjectsData[i] = new Object[]{warObjects.get(i).getKey() ^ id, warObject.getId()};
            }
        }
        jsonArray = (JsonArray) gson.toJsonTree(warObjectsData);
        return jsonArray;
    }

    public JsonArray getBeansJson(int id, boolean isForUI)
    {
        return getLocRelatedJson(turnBeanLocations, id, isForUI);
    }

    public JsonArray getNukesJson(int id, boolean isForUI)
    {
        return getLocRelatedJson(turnNukeLocations, id, isForUI);
    }

    private JsonArray getLocRelatedJson(ArrayList<Pair<Integer, Point>> turnLocations, int id, boolean isForUI)
    {
        Object[][] locationsData = new Object[turnLocations.size()][2];
        for (int i = 0; i < turnLocations.size(); i++)
        {
            Point location = turnLocations.get(i).getValue();
            if (isForUI)
            {
                locationsData[i] = new Object[]{turnLocations.get(i).getKey(), location};
            } else
            {
                locationsData[i] = new Object[]{turnLocations.get(i).getKey() ^ id, location};
            }
        }
        jsonArray = (JsonArray) gson.toJsonTree(locationsData);
        return jsonArray;
    }

    public JsonArray getArcherAttacksJson()
    {
        Object[][] archerAttacksData = new Object[turnArcherAttacks.size()][2];
        for (int i = 0; i < turnArcherAttacks.size(); i++)
        {
            Tower tower = turnArcherAttacks.get(i).getKey();
            Unit unit = turnArcherAttacks.get(i).getValue();
            archerAttacksData[i] = new Object[]{tower.getId(), unit.getId()};
        }
        jsonArray = (JsonArray) gson.toJsonTree(archerAttacksData);
        return jsonArray;
    }

    public JsonArray getCannonAttacksJson()
    {
        Object[][] cannonAttacksData = new Object[turnCannonAttacks.size()][2];
        for (int i = 0; i < turnCannonAttacks.size(); i++)
        {
            Tower tower = turnCannonAttacks.get(i).getKey();
            Unit unit = turnCannonAttacks.get(i).getValue();
            Point point = new Point(unit.getPath().getCells().get(unit.getCurrentCellIndex()).getX()
                    , unit.getPath().getCells().get(unit.getCurrentCellIndex()).getY());
            cannonAttacksData[i] = new Object[]{tower.getId(), point};
        }
        jsonArray = (JsonArray) gson.toJsonTree(cannonAttacksData);
        return jsonArray;
    }

    public void clear()
    {
        turnNewUnits = new ArrayList<>();
        turnNewTowers = new ArrayList<>();
        turnNewPartialTowers = new ArrayList<>();
        turnTowerUpdates = new ArrayList<>();
        turnPartialTowerUpdates = new ArrayList<>();

        turnUnitCasualties = new ArrayList<>();
        turnTowerCasualties = new ArrayList<>();

        turnNukeLocations = new ArrayList<>();
        turnBeanLocations = new ArrayList<>();

        turnArcherAttacks = new ArrayList<>();
        turnCannonAttacks = new ArrayList<>();

        turnSuccessfulUnits = new ArrayList<>();
    }
}
