package ir.sharif.aichallenge.server.thefinalbattle.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.engine.core.GameServer;
import ir.sharif.aichallenge.server.thefinalbattle.UI.HtmlViewer;
import ir.sharif.aichallenge.server.thefinalbattle.model.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.ability.Ability;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.AbilityType;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.Direction;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.GameState;
import ir.sharif.aichallenge.server.thefinalbattle.model.message.InitialMessage;
import ir.sharif.aichallenge.server.thefinalbattle.utils.AbilityTools;
import ir.sharif.aichallenge.server.thefinalbattle.utils.VisionTools;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class GameEngine {
    public static final int PICK_OFFSET = 4;
    public static final int NUM_OF_CAST_TURN = 1;

    private int totalMovePhases;
    private int killScore;
    private int objectiveZoneScore;
    private int maxAP;
    private int maxTurns;
    private int maxScore;
    private int maxOvertime;
    private int remainingOvertime;
    private OvertimeHandler overtimeHandler = new OvertimeHandler(this);

    private AtomicInteger currentTurn;
    private Player[] players = new Player[2];
    private GameState state;
    private Map<String, Hero> heroes;
    private Map<String, Ability> abilities;
    private ir.sharif.aichallenge.server.thefinalbattle.model.Map map;
    private VisionTools visionTools;
    private AbilityTools abilityTools;
    private List<CastedAbility> castedAbilities = new ArrayList<>();
    private Map<Hero, Ability> fortifiedHeroes;
    private Set<Hero> ruhFortifiedHeroes;
    private List<Hero> respawnedHeroes = new ArrayList<>();

    private JsonArray serverViewJsons = new JsonArray();
    private GraphicHandler graphicHandler = new GraphicHandler(this);
    private Random random = new Random();

    private AtomicInteger currentMovePhase;
    private Set<Hero> castedHeroes;

    private HtmlViewer viewer;
    private boolean view = false;


    public static void main(String[] args) throws InterruptedException {
        AtomicInteger currentTurn = new AtomicInteger(0);
        AtomicInteger currentMovePhase = new AtomicInteger(0);
        boolean view = Arrays.asList(args).contains("--view");
        int extraTime = extractExtraTime(args);
        GameServer gameServer = new GameServer(new GameHandler(currentTurn, currentMovePhase, view, extraTime), args,
                currentTurn, currentMovePhase);
        gameServer.start();
        gameServer.waitForFinish();
    }

    private static int extractExtraTime(String[] args) {
        int extraTime = 0;
        try {
            for (String arg : args) {
                if (!arg.startsWith("--extra=") && !arg.startsWith("--extra:")) {
                    continue;
                }
                extraTime = Integer.parseInt(arg.substring(8));
                return extraTime;
            }
        } catch (Exception e) {
            return extraTime;
        }

        return extraTime;
    }

    public void initialize(InitialMessage initialMessage, String mapName, String firstTeam, String secondTeam) {
        state = GameState.INIT;

        Map<String, Integer> gameConstants = initialMessage.getGameConstants();
        setGameConstants(gameConstants);
        initPlayers();

        ClientInitialCell[][] cells = initialMessage.getMap().getCells();
        map = new ir.sharif.aichallenge.server.thefinalbattle.model.Map();
        map.init(cells, players);
        visionTools = new VisionTools(map);
        abilityTools = new AbilityTools();
        abilityTools.setMap(map);
        abilityTools.setVisionTools(visionTools);

        setCellsVision();

        List<ClientAbilityConstants> abilityConstants = initialMessage.getAbilityConstants();
        initAbilities(abilityConstants);

        List<ClientHeroConstants> heroConstants = initialMessage.getHeroConstants();
        initHeroes(heroConstants);

        ruhFortifiedHeroes = new HashSet<>();

        JsonObject serverViewInit = Json.GSON.toJsonTree(initialMessage).getAsJsonObject();
        JsonObject serverViewGameConstants = serverViewInit.get("gameConstants").getAsJsonObject();
        serverViewGameConstants.addProperty("mapName", mapName);
        serverViewGameConstants.addProperty("firstTeam", firstTeam);
        serverViewGameConstants.addProperty("secondTeam", secondTeam);
        serverViewInit.remove("gameConstants");
        serverViewInit.add("gameConstants", serverViewGameConstants);

        serverViewJsons.add(serverViewInit);
        if (view) {
            viewer = new HtmlViewer();
        }
    }

    private void setCellsVision() {
        Cell[][] cells = map.getCells();
        List<Cell> cellList = new ArrayList<>();
        for (Cell[] rowCells : cells)
            cellList.addAll(Arrays.asList(rowCells));

        for (Cell cell : cellList) {
            Set<Cell> inVisionCells = new HashSet<>();
            for (Cell targetCell : cellList) {
                if (visionTools.isInVision(cell, targetCell))
                    inVisionCells.add(targetCell);
            }
            cell.setInVisionCells(inVisionCells);
        }
    }

    private void initPlayers() {
        for (int i = 0; i < players.length; i++) {
            Player player = new Player();
            player.setScore(0);
            player.setActionPoint(maxAP);
            players[i] = player;
        }

        players[0].setOpponent(players[1]);
        players[1].setOpponent(players[0]);
    }

    private void initHeroes(List<ClientHeroConstants> heroConstants) {
        heroes = new HashMap<>();
        for (ClientHeroConstants heroConstant : heroConstants) {
            List<Ability> heroAbilities = cloneAbilities(heroConstant.getAbilityNames());
            Hero hero = new Hero(heroConstant, heroAbilities);
            heroes.put(hero.getName(), hero);
        }
    }

    private List<Ability> cloneAbilities(String[] abilityNames) {
        List<Ability> wantedAbilities = new ArrayList<>();

        for (String abilityName : abilityNames) {
            try {
                wantedAbilities.add((Ability) abilities.get(abilityName).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        return wantedAbilities;
    }

    private void initAbilities(List<ClientAbilityConstants> abilityConstants) {
        abilities = new HashMap<>();

        for (ClientAbilityConstants abilityConstant : abilityConstants) {
            Ability ability = new Ability(abilityConstant);
            abilities.put(ability.getName(), ability);
        }
    }

    private void setGameConstants(Map<String, Integer> gameConstants) {
        this.killScore = gameConstants.get("killScore");
        this.objectiveZoneScore = gameConstants.get("objectiveZoneScore");
        this.maxAP = gameConstants.get("maxAP");
        this.maxTurns = gameConstants.get("maxTurns");
        this.maxScore = gameConstants.get("maxScore");
        this.maxOvertime = gameConstants.get("initOvertime");
        this.remainingOvertime = -1;
        this.totalMovePhases = gameConstants.get("totalMovePhases");
        OvertimeHandler.MAX_DIFF_SCORE = gameConstants.get("maxScoreDiff");
    }

    private void doPickTurn(String firstHero, String secondHero) {
        try {
            Hero hero = heroes.get(firstHero);
            if (hero != null)
                players[0].addHero((Hero) hero.clone());
            hero = heroes.get(secondHero);
            if (hero != null)
                players[1].addHero((Hero) hero.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void doTurn(ClientTurnMessage message1, ClientTurnMessage message2) {
        switch (state) {
            case INIT:
                break;
            case PICK:
                pick(message1, message2);
                break;
            case MOVE:
                move(message1, message2);
                break;
            case ACTION:
                cast(message1, message2);
                break;
            default:
                break;
        }
        resetCasters();
        updateKilledHeroes();
        assignScores();
        updateLogs();
        postProcess();
        updateStateAndTurn();
    }

    private void resetCasters() {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                hero.setHasCast(false);
            }
        }
    }

    private void postProcess() {
        if (state != GameState.ACTION)
            return;

        updateAbilityCooldowns();
        for (Player player : players) {
            player.setTotalUsedAp(player.getTotalUsedAp() + (maxAP - player.getActionPoint()));
            player.setActionPoint(maxAP);
        }
    }

    private void updateAbilityCooldowns() {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                for (Ability ability : hero.getAbilities()) {
                    if (ability.getRemainingCoolDown() > 0)
                        ability.setRemainingCoolDown(ability.getRemainingCoolDown() - 1);
                }
            }
        }
    }

    private void updateLogs() {
        if (state != GameState.PICK) {
            updateServerViewLog();
        }

        if (view) {
            int turn = state == GameState.PICK ? currentTurn.get() - 1 : currentTurn.get();
            viewer.updateData(turn, currentMovePhase.get(), state, castedAbilities, players, map);
            viewer.viewTurn();
        }
    }

    private void updateServerViewLog() {
        JsonObject log = new JsonObject();
        log.addProperty("currentTurn", currentTurn.get());
        log.addProperty("currentPhase", state.name());
        JsonArray castAbilitiesJson = getCastAbilitiesJson();
        log.add("castAbilities", castAbilitiesJson);
        JsonArray playersJson = new JsonArray();
        for (Player player : players) {
            player.updateServerViewLog(playersJson);
        }
        log.add("players", playersJson);
        serverViewJsons.add(log);
    }

    private JsonArray getCastAbilitiesJson() {
        JsonArray array = new JsonArray();
        for (CastedAbility castedAbility : castedAbilities) {
            JsonObject object = castedAbility.getJsonObject();
            array.add(object);
        }

        return array;
    }

    private void assignScores() {
        if (state != GameState.ACTION)
            return;

        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                if (map.getObjectiveZone().contains(hero.getCell())) {
                    player.setScore(player.getScore() + objectiveZoneScore);
                }
            }
        }
    }

    private void updateStateAndTurn() {
        int turn = currentTurn.get();
        if (turn >= PICK_OFFSET) {
            if (state == GameState.ACTION) {
                graphicHandler.addActionMessage();
                graphicHandler.addStatusMessage();
                currentTurn.incrementAndGet();
                overtimeHandler.updateOvertime();
                state = GameState.MOVE;
            } else if (state == GameState.MOVE) {
                currentMovePhase.set((currentMovePhase.get() + 1) % totalMovePhases);
                graphicHandler.addMoveMessage();
                if (currentMovePhase.get() == 0)
                    state = GameState.ACTION;
            } else {
                respawnAllHeroes();
                graphicHandler.addPickMessage();
                serverViewJsons.add(Json.GSON.toJsonTree(graphicHandler.getGraphicPickMessage(false)));
                state = GameState.MOVE;
                updatePlayerVisions();
            }
        } else {
            state = GameState.PICK;
        }
    }

    private void respawnAllHeroes() {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                respawnHero(hero, player);
            }
        }
    }

    private void cast(ClientTurnMessage firstMessage, ClientTurnMessage secondMessage) {
        ruhFortifiedHeroes = new HashSet<>();
        castedAbilities = new ArrayList<>();
        players[0].clearCastedAbilities();
        players[1].clearCastedAbilities();

        List<Cast> allFirstCasts = firstMessage.getCasts();
        List<Cast> allSecondCasts = secondMessage.getCasts();

        castAbilities(allFirstCasts, allSecondCasts, AbilityType.FORTIFY);
        castAbilities(allFirstCasts, allSecondCasts, AbilityType.DEFENSIVE);
        castAbilities(allFirstCasts, allSecondCasts, AbilityType.DODGE);
        castAbilities(allFirstCasts, allSecondCasts, AbilityType.OFFENSIVE);
    }

    private void castAbilities(List<Cast> allFirstCasts, List<Cast> allSecondCasts, AbilityType abilityType) {
        List<Cast> firstCasts = Cast.extractCasts(allFirstCasts, abilityType);
        List<Cast> secondCasts = Cast.extractCasts(allSecondCasts, abilityType);

        if (abilityType == AbilityType.DODGE) {
            castDodges(firstCasts, players[0]);
            castDodges(secondCasts, players[1]);

            castRemainingDodges(firstCasts, players[0]);
            castRemainingDodges(secondCasts, players[1]);

            affectNotLobbingDodges();
            return;
        }

        abilityTools.setMyHeroes(players[0].getHeroes());
        abilityTools.setOppHeroes(players[1].getHeroes());
        affectCasts(firstCasts, players[0]);

        abilityTools.setMyHeroes(players[1].getHeroes());
        abilityTools.setOppHeroes(players[0].getHeroes());
        affectCasts(secondCasts, players[1]);
    }

    private void affectNotLobbingDodges() {
        Collection<Hero> immuneHeroes = new HashSet<>(ruhFortifiedHeroes);
        for (CastedAbility castedAbility : castedAbilities) {
            if (castedAbility.getAbility().getType() == AbilityType.DODGE)
                immuneHeroes.add(castedAbility.getCasterHero());
        }

        for (CastedAbility castedAbility : castedAbilities) {
            Ability ability = castedAbility.getAbility();
            if (ability.getType() != AbilityType.DODGE || ability.isLobbing())
                continue;

            Player player;
            List<Hero> targetHeroes = new ArrayList<>();
            if (players[0].getHeroes().contains(castedAbility.getCasterHero()))
                player = players[0];
            else
                player = players[1];
            Cell[] rayCells = visionTools.getRayCells(castedAbility.getStartCell(), castedAbility.getEndCell(),
                    true);  //wall piercing not matters (there is no wall between start and end cell)
            for (Cell rayCell : rayCells) {
                List<Hero> heroes = rayCell.getHeroes();
                Set<Hero> deadHeroes = new HashSet<>();
                for (Hero hero : heroes) {
                    List<Cell> respawnZone = (player == players[0] ? map.getPlayer2RespawnZone() : map.getPlayer1RespawnZone());
                    if (immuneHeroes.contains(hero) || player.getHeroes().contains(hero) || respawnZone.contains(hero.getCell()))
                        continue;
                    hero.setHp(hero.getHp() - ability.getPower());
                    if (hero.getHp() <= 0)
                    {
                        deadHeroes.add(hero);
                    }
                    targetHeroes.add(hero);
                }

                for (Hero hero : deadHeroes)
                {
                    updateDashDeaths(player.getOpponent(), hero);
                }
            }
            castedAbility.setTargetHeroes(targetHeroes);
            addClientCastedAbility(castedAbility, player);  //update castAbilities for players
        }
    }

    private void castDodges(List<Cast> dodgeCasts, Player player) {
        DodgeHandler dodgeHandler = new DodgeHandler(map, player, extractTopFourDodges(dodgeCasts));
        Set<Cast> validDodgeCasts = dodgeHandler.getValidDodgeCasts();

        for (Cast cast : validDodgeCasts) {
            castDodgeCertain(cast, player);
        }
    }

    private void castRemainingDodges(List<Cast> dodgeCasts, Player player) {
        for (Cast cast : dodgeCasts) {
            if (cast.getHero().isHasCast() || cast.getAbility().getApCost() > player.getActionPoint())
                continue;
            Cell targetCell = map.getCell(cast.getTargetRow(), cast.getTargetColumn());
            Set<Cell> fullCells = new HashSet<>();
            for (Hero hero : player.getHeroes())
                if (hero.getCell() != null)
                    fullCells.add(hero.getCell());
            if (targetCell.isWall() || fullCells.contains(targetCell))
                continue;
            castDodgeCertain(cast, player);
        }
    }

    private List<Cast> extractTopFourDodges(List<Cast> dodgeCasts) {
        Set<Hero> chosenHeroes = new HashSet<>();
        List<Cast> ans = new ArrayList<>();
        for (Cast cast : dodgeCasts) {
            Hero hero = cast.getHero();
            if (hero.isHasCast() || chosenHeroes.contains(hero))
                continue;
            ans.add(cast);
            chosenHeroes.add(hero);
        }
        return ans;
    }

    private void castDodgeCertain(Cast cast, Player player) {
        castDodge(cast, player);
        player.setActionPoint(player.getActionPoint() - cast.getAbility().getApCost());
        cast.getAbility().setRemainingCoolDown(cast.getAbility().getCoolDown());
        cast.getHero().setHasCast(true);
        updatePlayerVisions();
    }

    private void affectCasts(List<Cast> casts, Player player) {
        int ap = player.getActionPoint();

        for (Cast cast : casts) {
            Hero hero = cast.getHero();
            Ability ability = cast.getAbility();
            int neededAP = ability.getApCost();
            int remCooldown = ability.getRemainingCoolDown();
            if (remCooldown != 0 || neededAP > ap || hero.isHasCast()) {
                continue;
            }

            Cell startCell = hero.getCell();
            Cell targetCell = map.getCell(cast.getTargetRow(), cast.getTargetColumn());
            if (startCell == null || targetCell == null)
                continue;

            ap -= neededAP;
            List<Hero> targetHeroes = Arrays.asList(abilityTools.getAbilityTargets(ability, startCell, targetCell));
            Cell realTarget = abilityTools.getImpactCell(ability, startCell, targetCell);
            hero.setHasCast(true);
            affectCast(hero, ability, realTarget, targetHeroes, player);
            ability.setRemainingCoolDown(ability.getCoolDown());
        }

        player.setActionPoint(ap);
    }

    private void affectCast(Hero caster, Ability ability, Cell realTarget, List<Hero> targetHeroes, Player player) {
        CastedAbility castedAbility = new CastedAbility();
        Cell casterCell = caster.getCell();

        switch (ability.getType()) {
            case DEFENSIVE:
                castDefensive(ability, targetHeroes);
                castedAbility.setAbility(ability);
                castedAbility.setStartCell(casterCell);
                castedAbility.setEndCell(realTarget);
                castedAbility.setCasterHero(caster);
                castedAbility.setTargetHeroes(targetHeroes);
                break;
            case OFFENSIVE:
                List<Hero> finalTargets = castOffensive(caster, ability, targetHeroes, player);
                castedAbility.setAbility(ability);
                castedAbility.setStartCell(casterCell);
                castedAbility.setEndCell(realTarget);
                castedAbility.setCasterHero(caster);
                castedAbility.setTargetHeroes(finalTargets);
                break;
            case FORTIFY:
                ruhFortifiedHeroes.addAll(targetHeroes);
                castedAbility.setAbility(ability);
                castedAbility.setStartCell(casterCell);
                castedAbility.setEndCell(realTarget);
                castedAbility.setCasterHero(caster);
                castedAbility.setTargetHeroes(targetHeroes);
                break;
        }
        castedAbilities.add(castedAbility);
        addClientCastedAbility(castedAbility, player);
    }

    private void castDefensive(Ability ability, List<Hero> targetHeroes)
    {
        for (Hero hero : targetHeroes) {
            int power = ability.getPower();
            int heroCurrentHP = hero.getHp();
            int emptyHP = hero.getMaxHp() - heroCurrentHP;
            int finalPower = Math.min(power, emptyHP);
            hero.setHp(heroCurrentHP + finalPower);
        }
    }

    private List<Hero> castOffensive(Hero caster, Ability ability, List<Hero> targetHeroes, Player player) {
        List<Hero> finalTargets = new ArrayList<>();
        Player opp = player.getOpponent();
        Cell casterCell = caster.getCell();

        for (Hero hero : targetHeroes) {
            Cell targetCell = hero.getCell();

            if (opp.getRespawnZone().contains(targetCell) || player.getRespawnZone().contains(casterCell) ||
                    ruhFortifiedHeroes.contains(hero)) {
                continue;
            }

            int power = ability.getPower();
            int hp = hero.getHp();
            hero.setHp(hp - power);
            finalTargets.add(hero);
        }

        return finalTargets;
    }

    private void move(ClientTurnMessage message1, ClientTurnMessage message2) {
        List<Move> allMoves1 = message1.getMoves();
        List<Move> allMoves2 = message2.getMoves();

        resetHeroesRecentPaths();
        castedAbilities = new ArrayList<>();
        players[0].clearCastedAbilities();
        players[1].clearCastedAbilities();

        List<Cast> moveDodgeCasts1 = createDodgeCastsFromMoves(allMoves1);
        List<Cast> moveDodgeCasts2 = createDodgeCastsFromMoves(allMoves2);

        castAbilities(moveDodgeCasts1, moveDodgeCasts2, AbilityType.DODGE);

//        for (Player player : players)
//        {
//            HashSet<Cell> set = new HashSet<>();
//
//            for (Hero hero : player.getHeroes())
//            {
//                Cell cell = hero.getCell();
//                if (set.contains(cell))
//                {
//                    System.out.println();
//                }
//
//                if (cell == null)
//                    continue;
//                set.add(cell);
//            }
//        }

        //these must be empty after move
        castedAbilities = new ArrayList<>();
        players[0].clearCastedAbilities();
        players[1].clearCastedAbilities();
    }

    private List<Cast> createDodgeCastsFromMoves(List<Move> moves) {
        List<Cast> dodgeCasts = new ArrayList<>();
        for (Move move : moves) {
            Hero hero = move.getHero();
            if (hero.getCell() == null)
                continue;

            Ability ability = new Ability();
            ability.setApCost(hero.getMoveApCost());
            ability.setRemainingCoolDown(0);
            ability.setCoolDown(0);             //not matters
            ability.setAreaOfEffect(0);         //not matters
            ability.setLobbing(true);
            ability.setName("move");            //not matters
            ability.setPiercing(false);         //not matters
            ability.setPower(0);                //not matters
            ability.setRange(1);
            ability.setType(AbilityType.DODGE);

            Cell cell = nextCellIfNotWall(hero.getCell(), move.getMoves().get(0));  //null pointer exception is handled in prepareClientMessage
            if (cell == null)
                continue;
            new Cast(hero, ability, cell.getRow(), cell.getColumn());
            dodgeCasts.add(new Cast(hero, ability, cell.getRow(), cell.getColumn()));
        }
        return dodgeCasts;
    }

    private void updatePlayerVisions() {
        for (Player player : players) {
            Set<Cell> vision = new HashSet<>();
            for (Hero hero : player.getHeroes()) {
                if (hero.getHp() == 0) {
                    continue;
                }
                vision.addAll(hero.getCell().getInVisionCells());
            }
            player.setVision(vision);
        }
    }

    private void resetHeroesRecentPaths() {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                hero.setRecentPath(new ArrayList<>());
                if (hero.getCell() == null)
                    continue;
                hero.getRecentPath().add(hero.getCell());
            }
        }
    }

    private void pick(ClientTurnMessage message1, ClientTurnMessage message2) {
        List<String> heroNames = new ArrayList<>(heroes.keySet());
        String firstPlayerHero = message1.getHeroName();
        String secondPlayerHero = message2.getHeroName();
        if (message1.getType() != GameState.PICK) {
            firstPlayerHero = heroNames.get(random.nextInt(heroes.size()));
        }
        if (message2.getType() != GameState.PICK) {
            secondPlayerHero = heroNames.get(random.nextInt(heroes.size()));
        }
        doPickTurn(firstPlayerHero, secondPlayerHero);
        currentTurn.incrementAndGet();
    }

    private void updateKilledHeroes() {
        if (state != GameState.ACTION)
            return;

        respawnedHeroes = new ArrayList<>();
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                if (hero.getHp() > 0) {
                    continue;
                }
                updateDeadHeroStats(player, hero);
            }
        }
    }

    private void updateDeadHeroStats(Player player, Hero hero) {
        hero.setHp(0);
        if (hero.getCell() != null) {
            hero.moveTo(null);
            hero.setRespawnTime(hero.getMaxRespawnTime());
            player.getOpponent().setScore(player.getOpponent().getScore() + killScore);
        }
        hero.setRespawnTime(hero.getRespawnTime() - 1);
        if (hero.getRespawnTime() <= 0) {
            respawnHero(hero, player);
        }
    }

    private void updateDashDeaths(Player player, Hero hero) {
        hero.setHp(0);
        hero.moveTo(null);
        hero.setRespawnTime(hero.getMaxRespawnTime() + 1);
        player.getOpponent().setScore(player.getOpponent().getScore() + killScore);
    }

    private void respawnHero(Hero hero, Player player) {
        Cell cell = getValidRespawnCell(player);
        hero.moveTo(cell);
        hero.resetValues();
        respawnedHeroes.add(hero);
    }

    private Cell getValidRespawnCell(Player player) {
        Cell cell = null;
        boolean isFinish = false;
        while (!isFinish) {
            Cell[] respawnZone = player.getRespawnZone().toArray(new Cell[0]);
            cell = respawnZone[Math.abs(new Random().nextInt() % respawnZone.length)];
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

    private void castDodge(Cast cast, Player player) {
        List<Hero> targetHeroes = new ArrayList<>();
        addDodgeCastedAbility(cast, player, targetHeroes);
        Hero hero = cast.getHero();
        Cell cell = map.getCell(cast.getTargetRow(), cast.getTargetColumn());
        hero.moveTo(cell);
        hero.getRecentPath().add(cell);
    }

    private void addClientCastedAbility(CastedAbility castedAbility, Player player) {
        ClientCastedAbility clientCastedAbility = new ClientCastedAbility();
        ClientCastedAbility clientOppCastedAbility = new ClientCastedAbility();

        clientCastedAbility.setCasterId(castedAbility.getCasterHero().getId());
        clientCastedAbility.setAbilityName(castedAbility.getAbility().getName());
        clientCastedAbility.setStartCell(new EmptyCell(castedAbility.getStartCell()));
        clientCastedAbility.setEndCell(new EmptyCell(castedAbility.getEndCell()));

        List<Integer> targetHeroIds = new ArrayList<>();
        for (Hero hero : castedAbility.getTargetHeroes()) {
            if (player.getVision().contains(hero.getCell())) {
                targetHeroIds.add(hero.getId());
            }
        }

        clientCastedAbility.setTargetHeroIds(targetHeroIds);
        player.getMyCastedAbilities().add(clientCastedAbility);

        Set<Cell> opponentVisions = player.getOpponent().getVision();
        clientOppCastedAbility.setCasterId(opponentVisions.contains(castedAbility.getCasterHero()
                .getCell()) ? castedAbility.getCasterHero().getId() : -1);
        clientOppCastedAbility.setAbilityName(castedAbility.getAbility().getName());
        clientOppCastedAbility.setEndCell(opponentVisions.contains(castedAbility.getEndCell()) ?
                new EmptyCell(castedAbility.getEndCell()) : null);
        clientOppCastedAbility.setStartCell(opponentVisions.contains(castedAbility.getStartCell()) ?
                new EmptyCell(castedAbility.getStartCell()) : null);

        targetHeroIds = new ArrayList<>();
        for (Hero hero : castedAbility.getTargetHeroes()) {
            if (player.getVision().contains(hero.getCell())) {
                targetHeroIds.add(hero.getId());
            }
        }
        clientOppCastedAbility.setTargetHeroIds(targetHeroIds);
        if (clientOppCastedAbility.getStartCell() != null || clientOppCastedAbility.getEndCell() != null ||
                clientOppCastedAbility.getTargetHeroIds().size() > 0)
            player.getOppCastedAbilities().add(clientOppCastedAbility);
    }

    private void addDodgeCastedAbility(Cast cast, Player player, List<Hero> targetHeroes) {
        CastedAbility castedAbility = new CastedAbility();
        castedAbility.setCasterHero(cast.getHero());
        castedAbility.setTargetHeroes(targetHeroes);
        castedAbility.setStartCell(cast.getHero().getCell());
        castedAbility.setAbility(cast.getAbility());
        castedAbility.setEndCell(map.getCell(cast.getTargetRow(), cast.getTargetColumn()));
        castedAbilities.add(castedAbility);
        if (cast.getAbility().isLobbing())      //if added for not lobbing dodges (they are handled later)
            addClientCastedAbility(castedAbility, player);
    }

    // deletes invalid moves
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

    public void close() {
        graphicHandler.close();
        if (view) {
            viewer.close();
        }
    }
}
