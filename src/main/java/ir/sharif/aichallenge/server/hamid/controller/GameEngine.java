package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.hamid.model.*;
import ir.sharif.aichallenge.server.hamid.model.enums.AbilityType;
import ir.sharif.aichallenge.server.hamid.model.enums.Direction;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;
import ir.sharif.aichallenge.server.hamid.utils.VisionTools;

import java.util.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GameEngine {
    public static final int PICK_OFFSET = 4;
    public static final int NUM_OF_MOVE_TURN = 4;
    public static final int NUM_OF_CAST_TURN = 4;

    private AtomicInteger currentTrun;


    private Player[] players = new Player[2];
    private GameState state;
    private Map<Integer, Hero> heroes;
    private ir.sharif.aichallenge.server.hamid.model.Map map;
    private VisionTools visionTools;

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
            players[0].addHero((Hero) heroes.get(firstHero).clone());
            players[1].addHero((Hero) heroes.get(secondHero).clone());
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
            //sort
            List<Move> moves1 = message1.getMoves(); //todo merge same hero moves in one move in getMoves //todo filter dead hero moves
            List<Move> moves2 = message2.getMoves();
            for (Move move : moves1) {
                prepareMove(move);
            }
            for (Move move : moves2) {
                prepareMove(move);
            }
            moves1.sort(Comparator.comparingInt(o -> o.getMoves().size()));
            moves2.sort(Comparator.comparingInt(o -> o.getMoves().size()));
            //post Prepare
            postPrepare(moves1);
            postPrepare(moves2);
            //set heroes recentPath
            for (Move move : moves1) {
                Hero hero = move.getHero();
                List<Cell> recentPath = new ArrayList<>();
                Cell cell = hero.getCell();
                recentPath.add(cell);
                for (Direction direction : move.getMoves()) {
                    cell = nextCellIfNotWall(cell, direction); //it's valid
                    recentPath.add(cell);
                }
                hero.setRecentPath(recentPath);
            }

            //move and vision
            int maxIter = 0;
            for (Player player : players) {
                for (Hero hero : player.getHeroes()) {
                    hero.setRecentPathForOpponent(new ArrayList<>());
                    maxIter = Math.max(maxIter, hero.getRecentPath().size());
                }
            }
            for (int i = 0; i < maxIter; i++) {
                for (Player player : players) {
                    for (Hero hero : player.getHeroes()) {
                        if (i < hero.getRecentPath().size()) {
                            hero.setCell(hero.getRecentPath().get(i)); // at the end of iteration heroes are at their destination
                        }
                    }
                }
                for (Hero firstPlayerHero : players[0].getHeroes()) {
                    for (Hero secondPlayerHero : players[1].getHeroes()) {
                        if (visionTools.isInVision(firstPlayerHero.getCell(), secondPlayerHero.getCell())) {
                            firstPlayerHero.addToRecentPathForOpponent(firstPlayerHero.getCell());
                            secondPlayerHero.addToRecentPathForOpponent(secondPlayerHero.getCell());
                        }
                    }
                }
            }
            // unique recentPathForOpponent
            for (Player player : players) {
                for (Hero hero : player.getHeroes()) {
                    List<Cell> path = hero.getRecentPathForOpponent();
                    List<Cell> ans = new ArrayList<>();
                    ans.add(path.get(0));
                    for (int i = 1; i < path.size(); i++) {
                        if (path.get(i) != path.get(i - 1))
                            ans.add(path.get(i));
                    }
                    hero.setRecentPathForOpponent(ans);
                }
            }
            // end of move and vision

            //vision for players
            for (Player player : players) {
                Set<Cell> vision = new HashSet<>();
                for (int i = 0; i < map.getNumberOfRows(); i++) {
                    for (int j = 0; j < map.getNumberOfColumns(); j++) {
                        Cell cell = map.getCell(i, j);
                        for (Hero hero : player.getHeroes()) {  // todo check alive
                            if (visionTools.isInVision(cell, hero.getCell())) {
                                vision.add(cell);
                            }
                        }
                    }
                }
                player.setVision(vision);
            }
            //end of vision for players
        }

        //cast
        if (state.equals(GameState.CAST)) {
            //todo cast
            List<Cast> casts1 = message1.getCasts();
            List<Cast> casts2 = message2.getCasts();

            for (Cast cast : casts1) {
                if (cast.getAbility().getType() == AbilityType.FORTIFY) {

                }
            }
            for (Cast cast : casts2) {
                if (cast.getAbility().getType() == AbilityType.FORTIFY) {

                }
            }

            for (Cast cast : casts1) {
                if (cast.getAbility().getType() == AbilityType.HEAL) {
                    if (visionTools.manhattanDistance(map.getCell(cast.getTargetRow() , cast.getTargetColumn()) , cast.getHero().getCell()) <= cast.getAbility().getAreaOfEffect()) {

                    }
                }
            }

            for (Cast cast : casts2) {
                if (cast.getAbility().getType() == AbilityType.HEAL) {

                }
            }

            for (Cast cast : casts1) {
                if (cast.getAbility().getType() == AbilityType.DODGE) {

                }
            }

            for (Cast cast : casts2) {
                if (cast.getAbility().getType() == AbilityType.DODGE) {

                }
            }

            for (Cast cast : casts1) {
                if (cast.getAbility().getType() == AbilityType.ATTACK) {

                }
            }

            for (Cast cast : casts2) {
                if (cast.getAbility().getType() == AbilityType.ATTACK) {

                }
            }

        }


        if (currentTrun.get() >= PICK_OFFSET) {
            int turn = currentTrun.get() - PICK_OFFSET;
            if (turn % (NUM_OF_CAST_TURN + NUM_OF_MOVE_TURN) < NUM_OF_MOVE_TURN) {
                state = GameState.MOVE;
            } else {
                state = GameState.CAST;
            }
        } else {
            state = GameState.PICK;
        }

        //todo assign scores
    }

    // check final location of heroes
    private void prepareMove(Move move) {
        Cell cell = move.getHero().getCell(); //not change cell
        List<Direction> newMoves = new ArrayList<>();
        for (Direction direction : move.getMoves()) {
            Cell nextCell = nextCellIfNotWall(cell, direction);
            if (nextCell != null) {
                cell = nextCell;
                newMoves.add(direction);
            }
        }
        move.setMoves(newMoves);
    }

    private Cell nextCellIfNotWall(Cell cell, Direction direction) {
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
        if (row >= 0 && row < map.getNumberOfRows() && column >= 0
                && column < map.getNumberOfColumns() && !map.getCell(row, column).isWall()) {
            return map.getCell(row, column);
        }
        return null;
    }

    private Cell getEmptyCell(int row, int column) {
        return new Cell(false, false, null, row, column);
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

    //this method used in postPrepare
    private void move(List<Cell> reservedCell, Move move, Hero hero, List<Direction> newPath) {
        for (Direction moveMove : move.getMoves()) {
            Cell oldCell = hero.getCell();
            Cell newCell;
            int newRow;
            int newColumn;
            switch (moveMove) {
                case UP:
                    newRow = oldCell.getRow() - 1;
                    newColumn = oldCell.getColumn();
                    newCell = getEmptyCell(newRow, newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.UP);
                    }
                    break;
                case DOWN:
                    newRow = oldCell.getRow() + 1;
                    newColumn = oldCell.getColumn();
                    newCell = getEmptyCell(newRow, newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.DOWN);
                    }
                    break;
                case LEFT:
                    newRow = oldCell.getRow();
                    newColumn = oldCell.getColumn() + 1;
                    newCell = getEmptyCell(newRow, newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.LEFT);
                    }
                    break;
                case RIGHT:
                    newRow = oldCell.getRow();
                    newColumn = oldCell.getColumn() - 1;
                    newCell = getEmptyCell(newRow, newColumn);
                    if (!reservedCell.contains(newCell)) {
                        hero.setCell(newCell);
                        newPath.add(Direction.RIGHT);
                    }
                    break;
            }
        }
    }
}
