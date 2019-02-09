package ir.sharif.aichallenge.server.thefinalbattle.utils;

import ir.sharif.aichallenge.server.thefinalbattle.model.Cell;
import ir.sharif.aichallenge.server.thefinalbattle.model.Hero;
import ir.sharif.aichallenge.server.thefinalbattle.model.Map;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.Direction;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class VisionTools {
    private Map map;

    public boolean isInVision(Cell startCell, Cell endCell) {
        if (startCell == null || endCell == null || startCell.isWall() || endCell.isWall())
            return false;
        Cell[] rayCells = getRayCells(startCell, endCell, false);
        Cell lastCell = rayCells[rayCells.length - 1];
        return lastCell == endCell;
    }

    public Cell[] getRayCells(Cell startCell, Cell targetCell, boolean wallPiercing) {
        ArrayList<Cell> path = new ArrayList<>();
        dfs(startCell, startCell, targetCell, new HashMap<>(), path, wallPiercing);
        return path.toArray(new Cell[0]);
    }

    private void dfs(Cell currentCell, Cell startCell, Cell targetCell, HashMap<Cell, Boolean> isSeen,
                     ArrayList<Cell> path, boolean wallPiercing) {
        isSeen.put(currentCell, true);
        path.add(currentCell);
        for (Direction direction : Direction.values()) {
            Cell nextCell = getNextCell(currentCell, direction);
            if (nextCell != null && !isSeen.containsKey(nextCell) && isCloser(currentCell, targetCell, nextCell)) {
                int collisionState = squareCollision(startCell, targetCell, nextCell);
                if ((collisionState == 0 || collisionState == 1) && (!wallPiercing && nextCell.isWall()))
                    return;
                if (collisionState == 1) {
                    dfs(nextCell, startCell, targetCell, isSeen, path, wallPiercing);
                    return;
                }
            }
        }
        for (int dRow = -1; dRow <= 1; dRow += 2)
            for (int dColumn = -1; dColumn <= 1; dColumn += 2) {
                int newRow = currentCell.getRow() + dRow;
                int newColumn = currentCell.getColumn() + dColumn;
                Cell nextCell = null;
                if (map.isInMap(newRow, newColumn)) nextCell = map.getCell(newRow, newColumn);
                if (nextCell != null && !isSeen.containsKey(nextCell) && isCloser(currentCell, targetCell, nextCell)) {
                    int collisionState = squareCollision(startCell, targetCell, nextCell);
                    if (collisionState == 0 || collisionState == 1 && (!wallPiercing && nextCell.isWall()))
                        return;
                    if (collisionState == 1) {
                        dfs(nextCell, startCell, targetCell, isSeen, path, wallPiercing);
                    }
                }
            }
    }

    private Cell getNextCell(Cell cell, Direction direction) {
        switch (direction) {
            case UP:
                if (map.isInMap(cell.getRow() - 1, cell.getColumn()))
                    return map.getCell(cell.getRow() - 1, cell.getColumn());
                else
                    return null;
            case DOWN:
                if (map.isInMap(cell.getRow() + 1, cell.getColumn()))
                    return map.getCell(cell.getRow() + 1, cell.getColumn());
                else
                    return null;
            case LEFT:
                if (map.isInMap(cell.getRow(), cell.getColumn() - 1))
                    return map.getCell(cell.getRow(), cell.getColumn() - 1);
                else
                    return null;
            case RIGHT:
                if (map.isInMap(cell.getRow(), cell.getColumn() + 1))
                    return map.getCell(cell.getRow(), cell.getColumn() + 1);
                else
                    return null;
        }
        return null; // never happens
    }

    private boolean isCloser(Cell currentCell, Cell targetCell, Cell nextCell) {
        return manhattanDistance(nextCell, targetCell) <= manhattanDistance(currentCell, targetCell);
    }

    public int manhattanDistance(Cell startCell, Cell endCell) {
        return Math.abs(startCell.getRow() - endCell.getRow()) + Math.abs(startCell.getColumn() - endCell.getColumn());
    }

    private int squareCollision(Cell startCell, Cell targetCell, Cell cell) {
        boolean hasNegative = false;
        boolean hasPositive = false;
        boolean hasZero = false;
        for (int row = 2 * cell.getRow(); row <= 2 * (cell.getRow() + 1); row += 2)
            for (int column = 2 * cell.getColumn(); column <= 2 * (cell.getColumn() + 1); column += 2) {
                int crossProduct = crossProduct(2 * startCell.getRow() + 1, 2 * startCell.getColumn() + 1,
                        2 * targetCell.getRow() + 1, 2 * targetCell.getColumn() + 1, row, column);
                if (crossProduct < 0) hasNegative = true;
                else if (crossProduct > 0) hasPositive = true;
                else hasZero = true;
            }
        if (hasNegative && hasPositive) return 1;
        if (hasZero) return 0;
        return -1;
    }

    private int crossProduct(int x1, int y1, int x2, int y2, int x3, int y3) {
        return (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
    }

    public Set<Cell> getHeroVision(Hero hero) {
        Set<Cell> vision = new HashSet<>();

        for (int i = 0; i < map.getNumberOfRows(); i++) {
            for (int j = 0; j < map.getNumberOfColumns(); j++) {
                Cell cell = map.getCell(i, j);
                if (isInVision(cell, hero.getCell())) {
                    vision.add(cell);
                }
            }
        }

        return vision;
    }
}
