package ir.sharif.aichallenge.TowerDefence.Map;

import ir.sharif.aichallenge.TowerDefence.GameObject.Tower;

/**
 * Created by msi1 on 1/21/2018.
 */
public class GrassCell extends Cell {
    private Tower tower;
    private boolean isConstructable;

    public GrassCell(int x, int y, boolean isConstructable) {
        super(x, y);
        this.isConstructable = isConstructable;
    }

    public void createTower(Tower tower) {
        if (this.tower == null && isConstructable) {
            this.tower = tower;
            isConstructable = false;
        }
    }

    public Tower plantBean()
    {
        isConstructable = false;
        Tower brokenTower = null;

        if (tower != null) {
            brokenTower = tower;
            tower = null;
        }

        return brokenTower;
    }

    public Tower getTower() {
        return tower;
    }

    public void setTower(Tower tower) {
        this.tower = tower;
    }

    public boolean isConstructable() {
        return isConstructable;
    }

    public boolean isConstructable(Cell[][] cells)
    {
        if (!isConstructable)
        {
            return false;
        }

        if (getX() != cells.length - 1 && cells[getX() + 1][getY()] instanceof GrassCell)
        {
            GrassCell neighbourCell = (GrassCell) cells[getX() + 1][getY()];
            if (neighbourCell.getTower() != null)
            {
                return false;
            }
        }

        if (getX() != 0 && cells[getX() - 1][getY()] instanceof GrassCell)
        {
            GrassCell neighbourCell = (GrassCell) cells[getX() - 1][getY()];
            if (neighbourCell.getTower() != null)
            {
                return false;
            }
        }

        if (getY() != cells[0].length - 1 && cells[getX()][getY() + 1] instanceof GrassCell)
        {
            GrassCell neighbourCell = (GrassCell) cells[getX()][getY() + 1];
            if (neighbourCell.getTower() != null)
            {
                return false;
            }
        }

        if (getY() != 0 && cells[getX()][getY() - 1] instanceof GrassCell)
        {
            GrassCell neighbourCell = (GrassCell) cells[getX()][getY() - 1];
            if (neighbourCell.getTower() != null)
            {
                return false;
            }
        }

        return true;
    }

    public void setConstructable(boolean constructable) {
        isConstructable = constructable;
    }
}
