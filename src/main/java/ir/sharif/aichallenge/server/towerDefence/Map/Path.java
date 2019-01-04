package ir.sharif.aichallenge.server.towerDefence.Map;

import java.util.ArrayList;

/**
 * Created by msi1 on 1/22/2018.
 */
public class Path
{
    private int id;
    private ArrayList<RoadCell> cells = new ArrayList<>();

    public Path(int id, ArrayList<RoadCell> cells)
    {
        this.id = id;
        this.cells = cells;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public ArrayList<RoadCell> getCells()
    {
        return cells;
    }

    public void setCells(ArrayList<RoadCell> cells)
    {
        this.cells = cells;
    }
}
