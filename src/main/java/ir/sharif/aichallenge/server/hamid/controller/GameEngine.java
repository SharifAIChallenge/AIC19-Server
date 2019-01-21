package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.hamid.model.*;
import ir.sharif.aichallenge.server.hamid.model.enums.Direction;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
            //todo move and vision
        }

        //cast
        if (state.equals(GameState.CAST)) {
            //todo cast
        }


        //todo check game state

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
