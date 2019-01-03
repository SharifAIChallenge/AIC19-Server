package ir.sharif.aichallenge.TowerDefence.GameObject;

import Factory.Constants;
import Map.Path;

/**
 * Created by msi1 on 1/21/2018.
 */
public class HeavyUnit extends Unit
{
    public HeavyUnit(int id, int level, Path path)
    {
        super(id, level, (int) Constants.UNITS_CONSTANTS[1][7],
                (int) (Constants.UNITS_CONSTANTS[1][2] * Math.pow(Constants.UNITS_CONSTANTS[1][3], (level - 1))),
                (int) Constants.UNITS_CONSTANTS[1][6], (int) Constants.UNITS_CONSTANTS[1][8],
                (int) (Constants.UNITS_CONSTANTS[1][4] + (double) (level - 1)*Constants.UNITS_CONSTANTS[1][5]), path);
    }
}
