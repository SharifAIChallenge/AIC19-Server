package ir.sharif.aichallenge.TowerDefence.Map;

import ir.sharif.aichallenge.TowerDefence.GameObject.Tower;
import ir.sharif.aichallenge.TowerDefence.GameObject.Unit;
import ir.sharif.aichallenge.TowerDefence.Factory.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by msi1 on 1/21/2018.
 */
public class Map
{
    private Cell[][] cells;
    private ArrayList<Path> paths = new ArrayList<>();
    private HashMap<Integer, Path> pathsMap = new HashMap<>();
    private int width;
    private int height;
    private JsonObject mapCellsData;
    private JsonArray pathsData;
    private int nukeRange;
    private Gson gson = new Gson();

    public Map(JsonObject mapCellsData, JsonArray pathsData)
    {
        this.mapCellsData = mapCellsData;
        this.pathsData = pathsData;
        this.nukeRange = Constants.RANGE_OF_NUKES;
        extractCellsData(mapCellsData);
        extractPathsData(pathsData);
    }

    private void extractPathsData(JsonArray pathsData)
    {
        for (int i = 0; i < pathsData.size(); i++)
        {
            JsonObject pathJson = (JsonObject) pathsData.get(i);
            int len = pathJson.get("len").getAsInt();
            JsonArray pathCells = pathJson.getAsJsonArray("cells");
            addPath(len, pathCells, i);
        }
    }

    private void addPath(int len, JsonArray pathCellsJson, int id)
    {
        ArrayList<RoadCell> pathCells = new ArrayList<>();

        for (int i = 0; i < len; i++)
        {
            JsonObject pathCell = (JsonObject) pathCellsJson.get(i);
            Point cellLocation = gson.fromJson(pathCell, Point.class);
            RoadCell cell = (RoadCell) cells[(int) cellLocation.getX()][(int) cellLocation.getY()];
            pathCells.add(cell);
        }

        Path path = new Path(id, pathCells);
        paths.add(path);
        pathsMap.put(id, path);
    }

    private void extractCellsData(JsonObject mapCellsData)
    {
        int[] size = gson.fromJson(mapCellsData.getAsJsonArray("size"), int[].class);
        String[] cells = gson.fromJson(mapCellsData.getAsJsonArray("cells"), String[].class);

        width = size[0];
        height = size[1];

        initCells(cells);
    }

    private void initCells(String[] cellsData)
    {
        cells = new Cell[width][height];

        for (int i = 0; i < height; i++)
        {
            String row = cellsData[i];

            for (int j = 0; j < width; j++)
            {
                char cellType = row.charAt(j);
                if (cellType == 'g')
                {
                    cells[j][i] = new GrassCell(j, i, true);
                } else if (cellType == 'r')
                {
                    cells[j][i] = new RoadCell(j, i);
                } else
                {
                    cells[j][i] = new GrassCell(j, i, false);
                }
            }
        }
    }

    public void addTower(Tower tower)
    {
        GrassCell cell = (GrassCell) cells[tower.getX()][tower.getY()];
        cell.createTower(tower);
    }

    public boolean isConstructableGrass(int x, int y)
    {
        if (x >= cells.length || x < 0 || y >= cells[0].length || y < 0)
        {
            return false;
        }
        Cell theChosenCell = cells[x][y];
        return (theChosenCell instanceof GrassCell) && ((GrassCell) theChosenCell).isConstructable(cells);
    }

    public Set<Unit> nuke(int x, int y)
    {
        if (x >= cells.length || x <= -1 || y >= cells[0].length || y <= -1)
        {
            return null;
        }

        Set<Unit> casualties = new HashSet<>();

        for (Cell[] rowCells : cells)
        {
            for (Cell cell : rowCells)
            {
                int cellX = cell.getX();
                int cellY = cell.getY();

                if (Math.abs(cellX - x) + Math.abs(cellY - y) <= nukeRange && cell instanceof RoadCell)
                {
                    casualties.addAll(((RoadCell) cell).destroy());
                }
            }
        }

        return casualties;
    }

    public Cell getCell(int x, int y)
    {
        if (x >= cells.length || x <= -1 || y >= cells[0].length || y <= -1)
        {
            return null;
        }
        return cells[x][y];
    }

    public Cell[][] getCells()
    {
        return cells;
    }

    public void setCells(Cell[][] cells)
    {
        this.cells = cells;
    }

    public ArrayList<Path> getPaths()
    {
        return paths;
    }

    public void setPaths(ArrayList<Path> paths)
    {
        this.paths = paths;
    }

    public HashMap<Integer, Path> getPathsMap()
    {
        return pathsMap;
    }

    public void setPathsMap(HashMap<Integer, Path> pathsMap)
    {
        this.pathsMap = pathsMap;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public JsonObject getMapCellsData()
    {
        return mapCellsData;
    }

    public JsonArray getPathsData()
    {
        return pathsData;
    }
}
