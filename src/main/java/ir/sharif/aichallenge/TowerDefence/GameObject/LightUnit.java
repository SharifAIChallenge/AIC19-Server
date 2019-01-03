package ir.sharif.aichallenge.TowerDefence.GameObject;

import ir.sharif.aichallenge.TowerDefence.Factory.Constants;
import ir.sharif.aichallenge.TowerDefence.Map.Path;

/**
 * Created by msi1 on 1/21/2018.
 */
public class LightUnit extends Unit
{
    public LightUnit(int id, int level, Path path)
    {
        super(id, level, (int) Constants.UNITS_CONSTANTS[0][7],
                (int) (Constants.UNITS_CONSTANTS[0][2] * Math.pow(Constants.UNITS_CONSTANTS[0][3], (level - 1))),
                (int) Constants.UNITS_CONSTANTS[0][6], (int) Constants.UNITS_CONSTANTS[0][8],
                (int) (Constants.UNITS_CONSTANTS[0][4] + (double) (level - 1)*Constants.UNITS_CONSTANTS[0][5]), path);


    }
}
