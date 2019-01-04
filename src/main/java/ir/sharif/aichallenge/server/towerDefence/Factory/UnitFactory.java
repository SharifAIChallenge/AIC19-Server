package ir.sharif.aichallenge.server.towerDefence.Factory;

import ir.sharif.aichallenge.server.towerDefence.Map.Path;
import ir.sharif.aichallenge.server.towerDefence.GameObject.Unit;
import ir.sharif.aichallenge.server.towerDefence.GameObject.HeavyUnit;
import ir.sharif.aichallenge.server.towerDefence.GameObject.LightUnit;

/**
 * Created by msi1 on 1/22/2018.
 */
public class UnitFactory
{
    private int leftoverMoney;
    private int creationIncome;

    private int lightUnitCost;
    private double lightUnitLevelUpCostInc;
    private int heavyUnitCost;
    private double heavyUnitLevelUpCostInc;

    private int lightUnitIncome;
    private int heavyUnitIncome;

    private int numToLevelUpLight;
    private int levelUpLightCounter;
    private int lightLevel = 1;

    private int numToLevelUpHeavy;
    private int levelUpHeavyCounter;
    private int heavyLevel = 1;

    private static int nextId = 1;

    public UnitFactory()
    {
        this.lightUnitCost = (int) Constants.UNITS_CONSTANTS[0][0];
        this.lightUnitLevelUpCostInc = Constants.UNITS_CONSTANTS[0][1];
        this.heavyUnitCost = (int) Constants.UNITS_CONSTANTS[1][0];
        this.heavyUnitLevelUpCostInc = Constants.UNITS_CONSTANTS[1][1];

        this.lightUnitIncome = (int) Constants.UNITS_CONSTANTS[0][10];
        this.heavyUnitIncome = (int) Constants.UNITS_CONSTANTS[1][10];

        this.numToLevelUpLight = (int) Constants.UNITS_CONSTANTS[0][9];
        this.levelUpLightCounter = (int) Constants.UNITS_CONSTANTS[0][9];

        this.numToLevelUpHeavy = (int) Constants.UNITS_CONSTANTS[1][9];
        this.levelUpHeavyCounter = (int) Constants.UNITS_CONSTANTS[1][9];
    }


    public Unit createUnit(Character type, Path path, int money)
    {
        if (type == 'l')
        {
            int cost = lightUnitCost + (int) ((double)(lightLevel - 1)*lightUnitLevelUpCostInc);

            if (money - cost >= 0)
            {
                Unit newUnit = new LightUnit(nextId++, lightLevel, path);
                leftoverMoney = money - cost;
                if (--levelUpLightCounter == 0)
                {
                    levelUpLightCounter = numToLevelUpLight;
                    lightLevel++;
                }
                creationIncome = lightUnitIncome;
                return newUnit;
            }
        } else if (type == 'h')
        {
            int cost = heavyUnitCost + (int) ((double) (heavyLevel - 1)*heavyUnitLevelUpCostInc);

            if (money - cost >= 0)
            {
                Unit newUnit = new HeavyUnit(nextId++, heavyLevel, path);
                leftoverMoney = money - cost;
                if (--levelUpHeavyCounter == 0)
                {
                    levelUpHeavyCounter = numToLevelUpHeavy;
                    heavyLevel++;
                }
                creationIncome = heavyUnitIncome;
                return newUnit;
            }
        }

        return null;
    }

    public int getLeftoverMoney()
    {
        return leftoverMoney;
    }

    public int getCreationIncome()
    {
        return creationIncome;
    }
}
