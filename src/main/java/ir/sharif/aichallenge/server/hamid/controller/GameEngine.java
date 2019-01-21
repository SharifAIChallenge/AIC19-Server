package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.hamid.model.*;
import ir.sharif.aichallenge.server.hamid.model.enums.Direction;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;

import java.util.*;
import java.util.Map;

public class GameEngine {

    public static final String PICK = "pick";
    private Player firstPlayer;
    private Player secondPlayer;
    private GameState state;
    private Map<Integer, Hero> heroes;
    private ir.sharif.aichallenge.server.hamid.model.Map map;

    public static void main(String[] args) {
        GameEngine gameEngine = new GameEngine();
//        gameEngine.initialize();
//        gameEngine.doPickTurn();
//        gameEngine.doTurn();
    }

    public void initialize() {
        // todo initialize heros
    }

    public void doPickTurn(int firstHero, int secondHero) {
        try {
            firstPlayer.addHero((Hero) heroes.get(firstHero).clone());
            secondPlayer.addHero((Hero) heroes.get(secondHero).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void doTurn(ClientTurnMessage message1, ClientTurnMessage message2) {
        //pick
        if (state.equals(GameState.PICK)) {
            if (message1.getType().equals(GameState.PICK) && message2.getType().equals(GameState.PICK))
                doPickTurn(message1.getHeroId(), message2.getHeroId());
            else if (message1.getType().equals(GameState.PICK)) {
                doPickTurn(message1.getHeroId(), Math.abs(new Random().nextInt()) % heroes.size());//todo random or null
            } else {
                doPickTurn(Math.abs(new Random().nextInt()) % heroes.size(), message2.getHeroId());
            }
        }

        //move
        if (state.equals(GameState.MOVE)) {
            //todo sort
            List<Move> moves1 = message1.getMoves();
            List<Move> moves2 = message2.getMoves();
            for (Move move: moves1) {
                prepareMove(move);
            }
            for (Move move: moves2) {
                prepareMove(move);
            }
            moves1.sort(Comparator.comparingInt(o -> o.getMoves().size()));
            moves2.sort(Comparator.comparingInt(o -> o.getMoves().size()));
            //todo move and vision
        }

        //cast
        if (state.equals(GameState.CAST)) {
            //todo cast
        }


        //todo check game state

    }

    private void prepareMove(Move move) {
        Cell cell = move.getHero().getCell(); // todo not change cell
        List<Direction> newMoves = new ArrayList<>();
        for (Direction direction: move.getMoves()) {
            if (isValid(cell, direction)) {
                cell = nextCell(cell, direction);
                newMoves.add(direction);
            }
        }
        move.setMoves(newMoves);
    }

    private boolean isValid(Cell cell, Direction direction) {
        int column = cell.getColumn();
        int row = cell.getRow();
        switch (direction) {
            case UP:
                row--;
                break;
            case DOWN:
                row++;
                break;
            case LEFT:
                column--;
                break;
            case RIGHT:
                column++;
                break;
        }
        return (row >= 0 && row < map.getNumberOfRows() && column >= 0
                && column < map.getNumberOfColumns() && !map.getCells()[row][column].isWall()); // todo 0 based ok?
    }

    private Cell nextCell(Cell cell, Direction direction) {
        int column = cell.getColumn();
        int row = cell.getRow();
        switch (direction) {
            case UP:
                row--;
                break;
            case DOWN:
                row++;
                break;
            case LEFT:
                column--;
                break;
            case RIGHT:
                column++;
                break;
        }
        return map.getCells()[row][column];
    }

    private Cell getEmptyCell(int row , int collumn) {
        return new Cell(false , false , null , row , collumn);
    }

    public void postPrepare(List<Move> moves) {
        List<Cell> reservedCell = new ArrayList<>();
        for (Move move : moves) {
            Hero hero = move.getHero();
            final Cell startCell = hero.getCell();
            List<Direction> newPath = new ArrayList<>();
            move(reservedCell, move, hero, newPath);
            move.setMoves(newPath);
            reservedCell.add(hero.getCell());
            hero.setCell(startCell);
        }
    }

    //this function used in postPrepare
    private void move(List<Cell> reservedCell, Move move, Hero hero, List<Direction> newPath) {
        for (Direction moveMove : move.getMoves()) {
            Cell oldCell = hero.getCell();
            Cell newCell;
            int newRow;
            int newColumn;
            switch (moveMove) {
                case UP:
                    newRow = oldCell.getRow() -1;
                    newColumn = oldCell.getColumn();
                    newCell = getEmptyCell(newRow , newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.UP);
                    }
                    break;
                case DOWN:
                    newRow = oldCell.getRow() +1;
                    newColumn = oldCell.getColumn();
                    newCell = getEmptyCell(newRow , newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.DOWN);
                    }
                    break;
                case LEFT:
                    newRow = oldCell.getRow() ;
                    newColumn = oldCell.getColumn() + 1;
                    newCell = getEmptyCell(newRow , newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.LEFT);
                    }
                    break;
                case RIGHT:
                    newRow = oldCell.getRow();
                    newColumn = oldCell.getColumn() - 1;
                    newCell = getEmptyCell(newRow , newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.RIGHT);
                    }
                    break;
            }
        }
    }
}
