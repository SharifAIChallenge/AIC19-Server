package ir.sharif.aichallenge.towerDefence.Factory;

import ir.sharif.aichallenge.towerDefence.GameObject.ArcherTower;
import ir.sharif.aichallenge.towerDefence.GameObject.CannonTower;
import ir.sharif.aichallenge.towerDefence.GameObject.Tower;

/**
 * Created by msi1 on 1/22/2018.
 */
public class TowerFactory {
    private static int nextId = 1;
    private int leftoverMoney;
    private int archerTowerCost;
    private int archerTowerCostInc = 5;
    private int cannonTowerCost;
    private int cannonTowerCostInc = 5;

    public TowerFactory() {
        this.archerTowerCost = (int) Constants.TOWERS_CONSTANTS[0][0];
        if (Constants.TOWERS_CONSTANTS[0].length == 9)
        {
            this.archerTowerCostInc = (int) Constants.TOWERS_CONSTANTS[0][8];
        }

        this.cannonTowerCost = (int) Constants.TOWERS_CONSTANTS[1][0];
        if (Constants.TOWERS_CONSTANTS[1].length == 9)
        {
            this.cannonTowerCostInc = (int) Constants.TOWERS_CONSTANTS[1][8];
        }
    }

    public static Tower createCopy(Tower tower) {
        if (tower instanceof ArcherTower) {
            return new ArcherTower(tower.getId(), tower.getX(), tower.getY(), tower.getLevel(), tower.getCost());
        }
        return new CannonTower(tower.getId(), tower.getX(), tower.getY(), tower.getLevel(), tower.getCost());
    }

    public static boolean isSameType(Tower firstTower, Tower secondTower) {
        return (firstTower instanceof ArcherTower && secondTower instanceof ArcherTower) ||
                (firstTower instanceof CannonTower && secondTower instanceof CannonTower);
    }

    public Tower createTower(Character type, int x, int y, int money) {
        if (type == 'a') {
            int cost = archerTowerCost;

            if (money - cost >= 0) {
                leftoverMoney = money - cost;
                archerTowerCost += archerTowerCostInc;
                return new ArcherTower(nextId++, x, y, 1, cost);
            }
        } else if (type == 'c') {
            int cost = cannonTowerCost;

            if (money - cost >= 0) {
                leftoverMoney = money - cost;
                cannonTowerCost += cannonTowerCostInc;
                return new CannonTower(nextId++, x, y, 1, cost);
            }
        }

        return null;
    }

    public int getLeftoverMoney() {
        return leftoverMoney;
    }

    public int getArcherTowerCost() {
        return archerTowerCost;
    }

    public void setArcherTowerCost(int archerTowerCost) {
        this.archerTowerCost = archerTowerCost;
    }

    public int getCannonTowerCost() {
        return cannonTowerCost;
    }

    public void setCannonTowerCost(int cannonTowerCost) {
        this.cannonTowerCost = cannonTowerCost;
    }
}
