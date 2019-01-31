package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.hamid.model.*;
import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.client.*;
import ir.sharif.aichallenge.server.hamid.model.enums.AbilityType;
import ir.sharif.aichallenge.server.hamid.model.enums.Direction;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;
import ir.sharif.aichallenge.server.hamid.model.message.InitialMessage;
import ir.sharif.aichallenge.server.hamid.utils.AbilityTools;
import ir.sharif.aichallenge.server.hamid.utils.VisionTools;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class GameEngine {
    public static final int PICK_OFFSET = 4;
    public static final int NUM_OF_MOVE_TURN = 1;
    public static final int NUM_OF_CAST_TURN = 1;

    private int killScore;
    private int objectiveZoneScore;
    private int maxAP;
    private int maxTurns;
    private int maxScore;

    private AtomicInteger currentTurn;
    private Player[] players = new Player[2];
    private GameState state;
    private Map<String, Hero> heroes;
    private Map<String, Ability> abilities;
    private ir.sharif.aichallenge.server.hamid.model.Map map;
    private VisionTools visionTools;
    private AbilityTools abilityTools;
    private List<List<ClientCastedAbility>> myCastedAbilities = new ArrayList<>(2); //list of cast and target hero //todo set value and check each hero use at most 1 ability and handle players ap
    private List<List<ClientCastedAbility>> oppCastedAbilities = new ArrayList<>(2);
    private List<CastedAbility> castedAbilities;
    // TODO fields below can be Player Class's fields
    private List<ClientCastedAbility> player1castedAbilities;
    private List<ClientCastedAbility> player2castedAbilities;
    private List<ClientCastedAbility> player1oppCastedAbilities;
    private List<ClientCastedAbility> player2oppCastedAbilities;
    private Map<Hero, Ability> fortifiedHeroes;

    public static void main(String[] args) {
        GameEngine gameEngine = new GameEngine();
//        gameEngine.initialize();
//        gameEngine.pick();
//        gameEngine.doTurn();
    }

    public void initialize(InitialMessage initialMessage) {
        currentTurn = new AtomicInteger();
        currentTurn.set(0);
        state = GameState.PICK;
        initPlayers();

        Map<String, Integer> gameConstants = initialMessage.getGameConstants();
        setGameConstants(gameConstants);

        ClientInitialCell[][] cells = initialMessage.getMap().getCells();
        map = new ir.sharif.aichallenge.server.hamid.model.Map();
        map.init(cells);
        visionTools = new VisionTools(map);
        abilityTools = new AbilityTools();
        abilityTools.setMap(map);
        abilityTools.setVisionTools(visionTools);

        List<ClientAbilityConstants> abilityConstants = initialMessage.getAbilityConstants();
        initAbilities(abilityConstants);

        List<ClientHeroConstants> heroConstants = initialMessage.getHeroConstants();
        initHeroes(heroConstants);
    }

    private void initPlayers()
    {
        for (int i = 0; i < players.length; i++)
        {
            Player player = new Player();
            player.setScore(0);
            player.setActionPoint(maxAP);
            players[i] = player;
        }

        players[0].setOpponent(players[1]);
        players[1].setOpponent(players[0]);
    }

    private void initHeroes(List<ClientHeroConstants> heroConstants)
    {
        heroes = new HashMap<>();
        for (ClientHeroConstants heroConstant : heroConstants)
        {
            List<Ability> heroAbilities = cloneAbilities(heroConstant.getAbilityNames());
            Hero hero = new Hero(heroConstant, heroAbilities);
            heroes.put(hero.getName(), hero);
        }
    }

    private List<Ability> cloneAbilities(String[] abilityNames)
    {
        List<Ability> wantedAbilities = new ArrayList<>();

        for (String abilityName : abilityNames)
        {
            try
            {
                wantedAbilities.add((Ability) abilities.get(abilityName).clone());
            } catch (CloneNotSupportedException e)
            {
                e.printStackTrace(); //TODO someone handle this pls
            }
        }

        return wantedAbilities;
    }

    private void initAbilities(List<ClientAbilityConstants> abilityConstants)
    {
        abilities = new HashMap<>();

        for (ClientAbilityConstants abilityConstant : abilityConstants)
        {
            Ability ability = new Ability(abilityConstant);
            abilities.put(ability.getName(), ability);
        }
    }

    private void setGameConstants(Map<String, Integer> gameConstants) {
        GameHandler.TURN_TIMEOUT = gameConstants.get("timeout");
        this.killScore = gameConstants.get("killScore");
        this.objectiveZoneScore = gameConstants.get("objectiveZoneScore");
        this.maxAP = gameConstants.get("maxAP");
        this.maxTurns = gameConstants.get("maxTurns");
        this.maxScore = gameConstants.get("maxScore");
    }

    private void doPickTurn(String firstHero, String secondHero) { // TODO check this
        try {
            players[0].addHero((Hero) heroes.get(firstHero).clone());
            players[1].addHero((Hero) heroes.get(secondHero).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void doTurn(ClientTurnMessage message1, ClientTurnMessage message2) {
        //pick
        pick(message1, message2);

        //move
        move(message1, message2);

        //cast
        cast(message1, message2);

        updateKilledHeroes();

        assignScores();

        changeStateAndTurn();
        // TODO put a reset method to make things cleaner
    }

    private void assignScores()
    {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                if (map.getObjectiveZone().contains(hero.getCell())) {      //todo List<Cell> in map --> Set<Cell> for contains
                    player.setScore(player.getScore() + objectiveZoneScore);
                }
            }
        }
    }

    private void changeStateAndTurn() {
        if (currentTurn.get() >= PICK_OFFSET) {
            if (state == GameState.CAST) {
                currentTurn.incrementAndGet();
                state = GameState.MOVE;
            } else {
                state = GameState.CAST;
            }
        } else {
            state = GameState.PICK;
        }
    }

    private void cast(ClientTurnMessage message1, ClientTurnMessage message2) {
         fortifiedHeroes = new HashMap<>();

        if (!state.equals(GameState.CAST)) {
            return;
        }

        List<Cast> casts1 = message1.getCasts();
        List<Cast> casts2 = message2.getCasts();

        // TODO implementing a reset method and clearing the lists there is better in my opinion
        player1castedAbilities = new ArrayList<>();
        player2castedAbilities = new ArrayList<>();
        player1oppCastedAbilities = new ArrayList<>();
        player2oppCastedAbilities = new ArrayList<>();
        castedAbilities = new ArrayList<>();

        castByAbility(casts1, casts2, AbilityType.FORTIFY);
        castByAbility(casts1, casts2, AbilityType.HEAL);
        List<Cast> validDodgeCasts1 = new ArrayList<>(new DodgeHandler(map, players[0], casts1).getValidDodgeCasts());
        List<Cast> validDodgeCasts2 = new ArrayList<>(new DodgeHandler(map, players[1], casts2).getValidDodgeCasts());
        castByAbility(validDodgeCasts1, validDodgeCasts2, AbilityType.DODGE);
        castByAbility(casts1, casts2, AbilityType.ATTACK);

/*
        List<Cast> casts = new ArrayList<>();
        casts.addAll(casts1);
        casts.addAll(casts2);
        Collections.sort(casts);
*/

/*
        for (Cast cast : casts) {
            if (casts1.contains(cast)) {
                abilityTools.setMyHeroes(players[0].getHeroes());
                abilityTools.setOppHeroes(players[1].getHeroes());
                cast(cast, 1, fortifiedHeroes);
            } else {
                abilityTools.setMyHeroes(players[1].getHeroes());
                abilityTools.setOppHeroes(players[0].getHeroes());
                cast(cast, 2, fortifiedHeroes);
            }
        }
*/


    }

    private void castByAbility(List<Cast> casts1, List<Cast> casts2, AbilityType abilityType) {  //todo clean this shit hole
        abilityTools.setMyHeroes(players[0].getHeroes());
        abilityTools.setOppHeroes(players[1].getHeroes());
        for (Cast cast : casts1) {
            if (cast.getAbility().getType() == abilityType) {
                if (cast.getAbility().getApCost() <= players[0].getActionPoint()) {
                    cast(cast, 1, fortifiedHeroes);
                    players[0].setActionPoint(players[0].getActionPoint() - cast.getAbility().getApCost());
                }
            }
        }
        abilityTools.setMyHeroes(players[1].getHeroes());
        abilityTools.setOppHeroes(players[0].getHeroes());
        for (Cast cast : casts2) {
            if (cast.getAbility().getType() == abilityType) {
                if (cast.getAbility().getApCost() <= players[1].getActionPoint()) {
                    cast(cast, 2, fortifiedHeroes);
                    players[1].setActionPoint(players[1].getActionPoint() - cast.getAbility().getApCost());
                }
            }
        }
    }

    private void move(ClientTurnMessage message1, ClientTurnMessage message2) {
        if (!state.equals(GameState.MOVE)) {
            return;
        }

        List<Move> moves1 = preprocessMessageMoves(message1);
        List<Move> moves2 = preprocessMessageMoves(message2);

        //set heroes recentPath
        updateHeroRecentPaths(moves1);
        updateHeroRecentPaths(moves2); // TODO ask ruhollah why is this here


        //move and vision
        int maxIter = 0;
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                hero.setRecentPathForOpponent(new ArrayList<>()); // TODO this can go to the reset method
                maxIter = Math.max(maxIter, hero.getRecentPath().size());
            }
        }
        for (int stepNumber = 0; stepNumber < maxIter; stepNumber++) {
            moveHeroesOneStep(stepNumber);
            updateHeroVisions();
        }
        updateOpponentHeroRecentPaths();

        // end of move and vision

        //vision for players
        updatePlayerVisions();
        //end of vision for players
    }

    private void updatePlayerVisions()
    {
        for (Player player : players) {
            Set<Cell> vision = new HashSet<>();
            for (Hero hero : player.getHeroes())
            {
                if (hero.getHp() == 0)
                {
                    continue;
                }
                vision.addAll(visionTools.getHeroVision(hero));
            }
            player.setVision(vision);
        }
    }

    private void updateOpponentHeroRecentPaths()
    {
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
    }

    private void updateHeroVisions()
    {
        for (Hero firstPlayerHero : players[0].getHeroes()) {
            for (Hero secondPlayerHero : players[1].getHeroes()) {
                if (visionTools.isInVision(firstPlayerHero.getCell(), secondPlayerHero.getCell())) {
                    firstPlayerHero.addToRecentPathForOpponent(firstPlayerHero.getCell());
                    secondPlayerHero.addToRecentPathForOpponent(secondPlayerHero.getCell());
                }
            }
        }
    }

    private void moveHeroesOneStep(int stepNumber)
    {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                if (stepNumber < hero.getRecentPath().size()) {
                    hero.moveTo(hero.getRecentPath().get(stepNumber)); // at the end of iteration heroes are at their destination
                }
            }
        }
    }

    private void updateHeroRecentPaths(List<Move> moves)
    {
        for (Move move : moves) {
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
    }

    private List<Move> preprocessMessageMoves(ClientTurnMessage message1)
    {
        message1.mergeMoves();
        List<Move> moves = message1.getMoves();
        for (Move move : moves) {
            prepareMove(move);
        }
        moves.sort(Comparator.comparingInt(o -> o.getMoves().size()));
        postPrepare(moves);

        return moves;
    }

    private void pick(ClientTurnMessage message1, ClientTurnMessage message2) { // TODO check this
        List<String> heroNames = new ArrayList<>(heroes.keySet());
        Random random = new Random(); // TODO this can even be a class field

        if (state.equals(GameState.PICK)) {
            if (message1.getType().equals(GameState.PICK) && message2.getType().equals(GameState.PICK))
                doPickTurn(message1.getHeroName(), message2.getHeroName());
            else if (message1.getType().equals(GameState.PICK)) {
                doPickTurn(message1.getHeroName(), heroNames.get(random.nextInt(heroes.size())));
            } else if (message2.getType().equals(GameState.PICK)) {
                doPickTurn(heroNames.get(random.nextInt(heroes.size())), message2.getHeroName());
            } else {
                doPickTurn(heroNames.get(random.nextInt(heroes.size())), heroNames.get(random.nextInt(heroes.size())));
            }
            currentTurn.incrementAndGet();
        }
    }

    private void updateKilledHeroes() {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                if (hero.getHp() > 0) {
                    continue;
                }
                updateDeadHeroStats(player, hero);
            }
        }
    }

    private void updateDeadHeroStats(Player player, Hero hero)
    {
        hero.setHp(0);
        if (hero.getCell() != null) {
            hero.moveTo(null);
            hero.setRespawnTime(hero.getMaxRespawnTime());
            player.getOpponent().setScore(player.getOpponent().getScore() + killScore);
        }
        hero.setRespawnTime(hero.getRespawnTime() - 1);
        if (hero.getRespawnTime() <= 0) {
            Cell cell = getValidRespawnCell(player);
            hero.moveTo(cell);
            hero.resetValues();
        }
    }

    private Cell getValidRespawnCell(Player player) {
        Cell cell = null;
        boolean isFinish = false;
        while (!isFinish) {
            cell = getRespawnZone(player).get(Math.abs(new Random().nextInt() % getRespawnZone(player).size()));
            isFinish = true;
            for (Hero cellHero : cell.getHeroes()) {
                if (player.getHeroes().contains(cellHero)) {
                    isFinish = false;
                    break;
                }
            }
        }
        return cell;
    }

    //this method was implemented because of bad models
    private List<Cell> getRespawnZone(Player player) {
        if (players[0].equals(player)) {
            return map.getPlayer1RespawnZone();
        }
        return map.getPlayer2RespawnZone();
    }

    private void cast(Cast cast, int player, Map<Hero, Ability> fortifiedHeroes) { // TODO this method needs serious cleaning
        AbilityType abilityType = cast.getAbility().getType();
        Ability ability = cast.getAbility();
        Player player1 = players[player - 1];
        Player opponent = player1.getOpponent();
        if (ability.getApCost() > player1.getActionPoint()) //todo clean this
        {
            return;
        }
        if (visionTools.manhattanDistance(map.getCell(cast.getTargetRow(), cast.getTargetColumn()),
                cast.getHero().getCell()) <= ability.getRange()) { // FIXME even if it's out of range it will hit the last cell possible

            List<Hero> targetHeroes = Arrays.asList(abilityTools.getAbilityTargets(ability, cast.getHero().getCell(),
                    map.getCell(cast.getTargetRow(), cast.getTargetColumn())));
            addCastedAbility(cast, player, targetHeroes);

            for (Hero hero : targetHeroes) {
                switch (abilityType) {
                    case HEAL:
                        if (player1.getHeroes().contains(hero)) {
                            int hp = Math.min(hero.getHp() + ability.getPower(), hero.getMaxHp());
                            hero.setHp(hp);
                        }
                        break;
                    case ATTACK:
                        if (player == 1)
                            player = 2;
                        else
                            player = 1;
                        if (players[player - 1].getHeroes().contains(hero)) {
                            if (player == 2) {
                                if (getRespawnZone(players[0]).contains(hero.getCell()))
                                    break;
                                if (getRespawnZone(players[1]).contains(cast.getHero().getCell()))
                                    break;
                            } else {
                                if (getRespawnZone(players[0]).contains(cast.getHero().getCell()))
                                    break;
                                if (getRespawnZone(players[1]).contains(hero.getCell()))
                                    break;
                            }
                            if (fortifiedHeroes.containsKey(hero)) {
                                if (ability.getPower() > fortifiedHeroes.get(hero).getPower()) {
                                    hero.setHp(hero.getHp() + fortifiedHeroes.get(hero).getPower() - ability.getPower());
                                }
                            } else
                                hero.setHp(hero.getHp() - ability.getPower());
                        }
                        break;
                    case FORTIFY:
                        if (players[player - 1].getHeroes().contains(hero)) {
                            fortifiedHeroes.put(hero, ability);
                        }
                        break;
                }
            }
            if (abilityType.equals(AbilityType.DODGE)) {    //todo check validation
                Hero hero = cast.getHero();
                hero.moveTo(map.getCell(cast.getTargetRow(), cast.getTargetColumn()));
            }
        }
    }

    private void addCastedAbility(Cast cast, int playerNum, List<Hero> targetHeroes) { // TODO what kind of a shit hell is this?
        CastedAbility castedAbility = new CastedAbility();
        castedAbility.setCasterHero(cast.getHero());
        castedAbility.setTargetHeroes(targetHeroes);
        castedAbility.setStartCell(cast.getHero().getCell());
        castedAbility.setAbility(cast.getAbility());
        castedAbility.setEndCell(abilityTools.getImpactCell(cast.getAbility(), cast.getHero().getCell(),
                map.getCell(cast.getTargetRow(), cast.getTargetColumn())));
        castedAbilities.add(castedAbility);

        ClientCastedAbility clientCastedAbility = new ClientCastedAbility();
        ClientCastedAbility clientOppCastedAbility = new ClientCastedAbility();
        if (playerNum == 1) {
            clientCastedAbility.setCasterId(castedAbility.getCasterHero().getId());
            clientCastedAbility.setAbilityName(castedAbility.getAbility().getName());
            clientCastedAbility.setStartCell(new EmptyCell(castedAbility.getStartCell()));
            clientCastedAbility.setEndCell(new EmptyCell(castedAbility.getEndCell()));
            List<Integer> targetHeroIds = new ArrayList<>();
            for (Hero hero : castedAbility.getTargetHeroes()) {
                if (players[0].getVision().contains(hero.getCell())) {
                    targetHeroIds.add(hero.getId());
                }
            }
            clientCastedAbility.setTargetHeroIds(targetHeroIds);
            player1castedAbilities.add(clientCastedAbility);

            clientOppCastedAbility.setCasterId(players[1].getVision().contains(castedAbility.getCasterHero().getCell()) ?
                    castedAbility.getCasterHero().getId() : -1);
            clientOppCastedAbility.setAbilityName(castedAbility.getAbility().getName());
            clientOppCastedAbility.setEndCell(players[1].getVision().contains(castedAbility.getEndCell()) ?
                    new EmptyCell(castedAbility.getEndCell()) : null);
            clientOppCastedAbility.setStartCell(players[1].getVision().contains(castedAbility.getStartCell()) ?
                    new EmptyCell(castedAbility.getStartCell()) : null);
            targetHeroIds = new ArrayList<>();
            for (Hero hero : castedAbility.getTargetHeroes()) {
                if (players[0].getVision().contains(hero.getCell())) {
                    targetHeroIds.add(hero.getId());
                }
            }
            clientOppCastedAbility.setTargetHeroIds(targetHeroIds);
            if (clientOppCastedAbility.getStartCell() != null || clientOppCastedAbility.getEndCell() != null ||
                    clientOppCastedAbility.getTargetHeroIds().size() > 0)
                player2oppCastedAbilities.add(clientOppCastedAbility);
        } else {
            clientCastedAbility.setCasterId(castedAbility.getCasterHero().getId());
            clientCastedAbility.setAbilityName(castedAbility.getAbility().getName());
            clientCastedAbility.setStartCell(new EmptyCell(castedAbility.getStartCell()));
            clientCastedAbility.setEndCell(new EmptyCell(castedAbility.getEndCell()));
            List<Integer> targetHeroIds = new ArrayList<>();
            for (Hero hero : castedAbility.getTargetHeroes()) {
                if (players[1].getVision().contains(hero.getCell())) {
                    targetHeroIds.add(hero.getId());
                }
            }
            clientCastedAbility.setTargetHeroIds(targetHeroIds);
            player2castedAbilities.add(clientCastedAbility);

            clientOppCastedAbility.setCasterId(players[0].getVision().contains(castedAbility.getCasterHero().getCell()) ?
                    castedAbility.getCasterHero().getId() : -1);
            clientOppCastedAbility.setAbilityName(castedAbility.getAbility().getName());
            clientOppCastedAbility.setEndCell(players[0].getVision().contains(castedAbility.getEndCell()) ?
                    new EmptyCell(castedAbility.getEndCell()) : null);
            clientOppCastedAbility.setStartCell(players[0].getVision().contains(castedAbility.getStartCell()) ?
                    new EmptyCell(castedAbility.getStartCell()) : null);
            targetHeroIds = new ArrayList<>();
            for (Hero hero : castedAbility.getTargetHeroes()) {
                if (players[1].getVision().contains(hero.getCell())) {
                    targetHeroIds.add(hero.getId());
                }
            }
            clientOppCastedAbility.setTargetHeroIds(targetHeroIds);
            if (clientOppCastedAbility.getStartCell() != null || clientOppCastedAbility.getEndCell() != null ||
                    clientOppCastedAbility.getTargetHeroIds().size() > 0)
                player1oppCastedAbilities.add(clientOppCastedAbility);
        }
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

    private Cell getEmptyCell(int row, int column) { // TODO static method for cell
        return new Cell(false, false, null, row, column);
    }

    private void postPrepare(List<Move> moves) {
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
    private void move(List<Cell> reservedCell, Move move, Hero hero, List<Direction> newPath) { // TODO this method should go to the Hero class
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
