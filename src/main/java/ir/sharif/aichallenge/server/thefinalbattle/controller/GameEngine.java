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
import ir.sharif.aichallenge.server.thefinalbattle.utils.HeroMovement;
import ir.sharif.aichallenge.server.thefinalbattle.utils.MovementHandler;
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
    public static final int NUM_OF_MOVE_TURN = 6;
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
    private ir.sharif.aichallenge.server.thefinalbattle.model.Map map;
    private VisionTools visionTools;
    private AbilityTools abilityTools;
    private List<CastedAbility> castedAbilities = new ArrayList<>();
    // TODO fields below can be Player Class's fields
    private List<ClientCastedAbility> player1castedAbilities = new ArrayList<>();   //todo list<list>
    private List<ClientCastedAbility> player2castedAbilities = new ArrayList<>();
    private List<ClientCastedAbility> player1oppCastedAbilities = new ArrayList<>();
    private List<ClientCastedAbility> player2oppCastedAbilities = new ArrayList<>();
    private Map<Hero, Ability> fortifiedHeroes;
    private Set<Hero> ruhFortifiedHeroes;
    private List<Hero> respawnedHeroes = new ArrayList<>();

    private JsonArray serverViewJsons = new JsonArray();
    private GraphicHandler graphicHandler = new GraphicHandler(this);
    private Random random = new Random();
    private AtomicInteger currentMovePhase;
    private Set<Hero> castedHeroes; //todo initialize

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

    private static int extractExtraTime(String[] args)
    {
        int extraTime = 0;
        try
        {
            for (String arg : args)
            {
                if (!arg.startsWith("--extra=") && !arg.startsWith("--extra:"))
                {
                    continue;
                }
                extraTime = Integer.parseInt(arg.substring(8));
                return extraTime;
            }
        } catch (Exception e)
        {
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

        List<ClientAbilityConstants> abilityConstants = initialMessage.getAbilityConstants();
        initAbilities(abilityConstants);

        List<ClientHeroConstants> heroConstants = initialMessage.getHeroConstants();
        initHeroes(heroConstants);

        JsonObject serverViewInit = Json.GSON.toJsonTree(initialMessage).getAsJsonObject();
        JsonObject serverViewGameConstants = serverViewInit.get("gameConstants").getAsJsonObject();
        serverViewGameConstants.addProperty("mapName", mapName);
        serverViewGameConstants.addProperty("firstTeam", firstTeam);
        serverViewGameConstants.addProperty("secondTeam", secondTeam);
        serverViewInit.remove("gameConstants");
        serverViewInit.add("gameConstants", serverViewGameConstants);

        serverViewJsons.add(serverViewInit);
        if (view)
        {
            viewer = new HtmlViewer();
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
                e.printStackTrace(); //TODO someone handle this pls
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
//        GameHandler.TURN_TIMEOUT = gameConstants.get("timeout");
        this.killScore = gameConstants.get("killScore");
        this.objectiveZoneScore = gameConstants.get("objectiveZoneScore");
        this.maxAP = gameConstants.get("maxAP");
        this.maxTurns = gameConstants.get("maxTurns");
        this.maxScore = gameConstants.get("maxScore");
    }

    private void doPickTurn(String firstHero, String secondHero) { // TODO check this
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
        //pick
        pick(message1, message2);

        //move
        move(message1, message2);

        //cast
        cast(message1, message2);

        resetCasters();

        updateKilledHeroes();

        assignScores();

        updateLogs();

        postProcess();

        updateStateAndTurn();
    }

    private void resetCasters()
    {
        for (Player player : players)
        {
            for (Hero hero : player.getHeroes())
            {
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

        if (view)
        {
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
                if (map.getObjectiveZone().contains(hero.getCell())) {      //todo List<Cell> in map --> Set<Cell> for contains
                    player.setScore(player.getScore() + objectiveZoneScore);
                }
            }
        }
    }

    private void updateStateAndTurn() {
        int turn = currentTurn.get();
//        System.out.println(turn);
        if (turn >= PICK_OFFSET) {
//            System.out.println(state);
            if (state == GameState.ACTION) {
                graphicHandler.addActionMessage();
                graphicHandler.addStatusMessage();
                currentTurn.incrementAndGet();
                state = GameState.MOVE;
            } else if (state == GameState.MOVE) {
                currentMovePhase.set((currentMovePhase.get() + 1) % NUM_OF_MOVE_TURN);
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

    private void cast(ClientTurnMessage firstMessage, ClientTurnMessage secondMessage)
    {
        if (!state.equals(GameState.ACTION))
        {
            return;
        }
        ruhFortifiedHeroes = new HashSet<>();
        castedAbilities = new ArrayList<>();
        player1castedAbilities = new ArrayList<>();
        player2castedAbilities = new ArrayList<>();
        player1oppCastedAbilities = new ArrayList<>();
        player2oppCastedAbilities = new ArrayList<>();

        List<Cast> allFirstCasts = firstMessage.getCasts();
        List<Cast> allSecondCasts = secondMessage.getCasts();

        castAbilities(allFirstCasts, allSecondCasts, AbilityType.FORTIFY);
        castAbilities(allFirstCasts, allSecondCasts, AbilityType.DEFENSIVE);
        castAbilities(allFirstCasts, allSecondCasts, AbilityType.DODGE);
        castAbilities(allFirstCasts, allSecondCasts, AbilityType.OFFENSIVE);
    }

    private void castAbilities(List<Cast> allFirstCasts, List<Cast> allSecondCasts, AbilityType abilityType)
    {
        List<Cast> firstCasts = Cast.extractCasts(allFirstCasts, abilityType);
        List<Cast> secondCasts = Cast.extractCasts(allSecondCasts, abilityType);

        if (abilityType == AbilityType.DODGE)
        {
            castDodges(firstCasts, players[0]);
            castDodges(secondCasts, players[1]);

            castRemainingDodges(firstCasts, players[0]);
            castRemainingDodges(secondCasts, players[1]);


            /*List<HeroMovement> firstMovements = affectDodges(firstCasts, players[0]);
            List<HeroMovement> secondMovements = affectDodges(secondCasts, players[1]);
            handleAllMovements(firstMovements, secondMovements);*/
            return;
        }

        abilityTools.setMyHeroes(players[0].getHeroes());
        abilityTools.setOppHeroes(players[1].getHeroes());

        affectCasts(firstCasts, players[0]);

        abilityTools.setMyHeroes(players[1].getHeroes());
        abilityTools.setOppHeroes(players[0].getHeroes());
        affectCasts(secondCasts, players[1]);
    }

    private void castDodges(List<Cast> dodgeCasts, Player player) {
        DodgeHandler dodgeHandler = new DodgeHandler(map, player, extractTopFourDodges(dodgeCasts, player));
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

    private List<Cast> extractTopFourDodges(List<Cast> dodgeCasts, Player player) {
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
        cast(cast, player, fortifiedHeroes);    //fortifiedHeroes not important here
        player.setActionPoint(player.getActionPoint() - cast.getAbility().getApCost());
        cast.getAbility().setRemainingCoolDown(cast.getAbility().getCoolDown());
        cast.getHero().setHasCast(true);    //todo asap is it enough?
        updatePlayerVisions();
    }

    private List<HeroMovement> affectDodges(List<Cast> dodgingCasts, Player player)
    {
        List<HeroMovement> dodgeMovements = createMovementsFromDodge(dodgingCasts, player);
        List<HeroMovement> validMovements = MovementHandler.getValidMovements(dodgeMovements, map, player);
        List<HeroMovement> finalMovements = fixMoveMessage(validMovements, player);

        for (HeroMovement movement : finalMovements)
        {
            CastedAbility castedAbility = new CastedAbility();
            castedAbility.setAbility(movement.getAbility());
            castedAbility.setTargetHeroes(new ArrayList<>());
            castedAbility.setCasterHero(movement.getHero());
            castedAbility.setStartCell(movement.getStartCell());
            castedAbility.setEndCell(movement.getEndCell());
            castedAbilities.add(castedAbility);
            addCastedAbility(castedAbility, player == players[0] ? 1 : 2);
            movement.getHero().setHasCast(true);
        }

        return finalMovements;
    }

    private List<HeroMovement> createMovementsFromDodge(List<Cast> dodgingCasts, Player player)
    {
        List<HeroMovement> movements = new ArrayList<>();

        for (Cast cast : dodgingCasts)
        {
            Hero hero = cast.getHero();
            Ability ability = cast.getAbility();
            int remCooldown = ability.getRemainingCoolDown();
            if (remCooldown != 0 || hero.isHasCast())
            {
                continue;
            }

            Cell startCell = hero.getCell();
            Cell targetCell = map.getCell(cast.getTargetRow(), cast.getTargetColumn());

            HeroMovement movement = new HeroMovement(startCell, targetCell, hero);
            movement.setAp(cast.getAbility().getApCost());
            movement.setAbility(ability);
            movements.add(movement);
        }

        for (Hero hero : player.getHeroes())
        {
            Cell currentCell = hero.getCell();
            if (currentCell == null)
                continue;
            HeroMovement noMove = new HeroMovement(currentCell, currentCell, hero);
            noMove.setAp(0);
            movements.add(noMove);
        }

        return movements;
    }

    private void affectCasts(List<Cast> casts, Player player)
    {
        int ap = player.getActionPoint();

        for (Cast cast : casts)
        {
            Hero hero = cast.getHero();
            Ability ability = cast.getAbility();
            int neededAP = ability.getApCost();
            int remCooldown = ability.getRemainingCoolDown();
            if (remCooldown != 0 || neededAP > ap || hero.isHasCast())
            {
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

    private void affectCast(Hero caster, Ability ability, Cell realTarget, List<Hero> targetHeroes, Player player)
    {
        CastedAbility castedAbility = new CastedAbility();;
        Cell casterCell = caster.getCell();

        switch (ability.getType())
        {
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
        addCastedAbility(castedAbility, player == players[0] ? 1 : 2);
    }

    private void castDefensive(Ability ability, List<Hero> targetHeroes) // TODO should we check if the hero belongs to a specific player?
    {
        for (Hero hero : targetHeroes)
        {
            int power = ability.getPower();
            int heroCurrentHP = hero.getHp();
            int emptyHP = hero.getMaxHp() - heroCurrentHP;
            int finalPower = Math.min(power, emptyHP);
            hero.setHp(heroCurrentHP + finalPower);
        }
    }

    private List<Hero> castOffensive(Hero caster, Ability ability, List<Hero> targetHeroes, Player player)
    {
        List<Hero> finalTargets = new ArrayList<>();
        Player opp = player.getOpponent();
        Cell casterCell = caster.getCell();

        for (Hero hero : targetHeroes)
        {
            Cell targetCell = hero.getCell();

            if (opp.getRespawnZone().contains(targetCell) || player.getRespawnZone().contains(casterCell) ||
                    ruhFortifiedHeroes.contains(hero))
            {
                continue;
            }

            int power = ability.getPower();
            int hp = hero.getHp();
            hero.setHp(hp - power);
            finalTargets.add(hero);
        }

        return finalTargets;
    }

//    private void cast(ClientTurnMessage message1, ClientTurnMessage message2) {
//        fortifiedHeroes = new HashMap<>();
//
//        if (!state.equals(GameState.ACTION)) {
//            return;
//        }
//
//        List<Cast> casts1 = message1.getCasts();
//        List<Cast> casts2 = message2.getCasts();
//
//        casts1 = filterCasts(casts1, players[0]);
//        casts2 = filterCasts(casts2, players[1]);
//
//        // TODO implementing a reset method and clearing the lists there is better in my opinion
//        player1castedAbilities = new ArrayList<>();
//        player2castedAbilities = new ArrayList<>();
//        player1oppCastedAbilities = new ArrayList<>();
//        player2oppCastedAbilities = new ArrayList<>();
//        castedAbilities = new ArrayList<>();
//
//        castByAbility(casts1, casts2, AbilityType.FORTIFY);
//        castByAbility(casts1, casts2, AbilityType.DEFENSIVE);
//        List<Cast> validDodgeCasts1 = new ArrayList<>(new DodgeHandler(map, players[0], casts1).getValidDodgeCasts());
//        List<Cast> validDodgeCasts2 = new ArrayList<>(new DodgeHandler(map, players[1], casts2).getValidDodgeCasts());
//        castByAbility(validDodgeCasts1, validDodgeCasts2, AbilityType.DODGE);
//        castByAbility(casts1, casts2, AbilityType.OFFENSIVE);
//
//        updatePlayerVisions();  //because of dodges TODO any extra action needed?
//
///*
//        List<Cast> casts = new ArrayList<>();
//        casts.addAll(casts1);
//        casts.addAll(casts2);
//        Collections.sort(casts);
//*/
//
///*
//        for (Cast cast : casts) {
//            if (casts1.contains(cast)) {
//                abilityTools.setMyHeroes(players[0].getHeroes());
//                abilityTools.setOppHeroes(players[1].getHeroes());
//                cast(cast, 1, fortifiedHeroes);
//            } else {
//                abilityTools.setMyHeroes(players[1].getHeroes());
//                abilityTools.setOppHeroes(players[0].getHeroes());
//                cast(cast, 2, fortifiedHeroes);
//            }
//        }
//*/
//
//
//    }

    private List<Cast> filterCasts(List<Cast> casts, Player player) {
        Set<Hero> seenHeroes = new HashSet<>();
        List<Cast> ans = new ArrayList<>();
        for (Cast cast : casts) {
            if (player.getHeroes().contains(cast.getHero()) && cast.getAbility().getRemainingCoolDown() == 0 &&
                    !seenHeroes.contains(cast.getHero()) &&
                    map.getCell(cast.getTargetRow(), cast.getTargetColumn()) != null) {
                ans.add(cast);
                seenHeroes.add(cast.getHero());
            }
        }
        return ans;
    }

    private void castByAbility(List<Cast> casts1, List<Cast> casts2, AbilityType abilityType) {  //todo clean this shit hole
        abilityTools.setMyHeroes(players[0].getHeroes());
        abilityTools.setOppHeroes(players[1].getHeroes());
        for (Cast cast : casts1) {
            if (cast.getAbility().getType() == abilityType) {
                if (cast.getAbility().getApCost() <= players[0].getActionPoint()) {
                    cast(cast, 1, fortifiedHeroes);
                    players[0].setActionPoint(players[0].getActionPoint() - cast.getAbility().getApCost());
                    cast.getAbility().setRemainingCoolDown(cast.getAbility().getCoolDown());
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
                    cast.getAbility().setRemainingCoolDown(cast.getAbility().getCoolDown());
                }
            }
        }
    }

    private void move(ClientTurnMessage message1, ClientTurnMessage message2) {
        if (!state.equals(GameState.MOVE)) {
            return;
        }

        //todo castAbilities(List<Cast> allFirstCasts, List<Cast> allSecondCasts, AbilityType abilityType)
        List<Move> allMoves1 = message1.getMoves();
        List<Move> allMoves2 = message2.getMoves();

        resetHeroesRecentPaths();
        List<Cast> moveDodgeCasts1 = createDodgeCastsFromMoves(allMoves1, players[0]);
        List<Cast> moveDodgeCasts2 = createDodgeCastsFromMoves(allMoves2, players[1]);

        castAbilities(moveDodgeCasts1, moveDodgeCasts2, AbilityType.DODGE);

        //these must be empty after move
        castedAbilities = new ArrayList<>();
        player1castedAbilities = new ArrayList<>();
        player2castedAbilities = new ArrayList<>();
        player1oppCastedAbilities = new ArrayList<>();
        player2oppCastedAbilities = new ArrayList<>();

        /*List<HeroMovement> moves1 = preprocessMessageMoves(message1, players[0]);
        List<HeroMovement> moves2 = preprocessMessageMoves(message2, players[1]);
        handleAllMovements(moves1, moves2);*/

//        //set heroes recentPath
//        resetHeroesRecentPaths();
//        updateHeroRecentPaths(moves1);
//        updateHeroRecentPaths(moves2); // TODO ask ruhollah why is this here
//
//
//        //move and vision
//        int maxIter = 0;
//        for (Player player : players) {
//            for (Hero hero : player.getHeroes()) {
//                hero.setRecentPathForOpponent(new ArrayList<>()); // TODO this can go to the reset method
//                maxIter = Math.max(maxIter, hero.getRecentPath().size());
//            }
//        }
//        for (int stepNumber = 0; stepNumber < maxIter; stepNumber++) {
//            moveHeroesOneStep(stepNumber); // TODO check move ap on prepare
//            updateHeroVisions();
//        }
//        updateOpponentHeroRecentPaths();
//
//        // end of move and vision
//
//        //vision for players
//        updatePlayerVisions();
//        //end of vision for players
    }

    private List<Cast> createDodgeCastsFromMoves(List<Move> moves, Player player) {
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
            ability.setName("move");    //not matters
            ability.setPiercing(false);         //not matters
            ability.setPower(0);                //not matters
            ability.setRange(1);
            ability.setType(AbilityType.DODGE);

            Cell cell = nextCellIfNotWall(hero.getCell(), move.getMoves().get(0));  //null pointer exception is handled in prepareClientMessage
            if (cell == null)
                continue;

            dodgeCasts.add(new Cast(hero, ability, cell.getRow(), cell.getColumn()));
        }
        return dodgeCasts;
    }

    private void handleAllMovements(List<HeroMovement> firstMovements, List<HeroMovement> secondMovements)
    {
        //set heroes recentPath
        resetHeroesRecentPaths();
        updateHeroRecentPaths(firstMovements);
        updateHeroRecentPaths(secondMovements); // TODO ask ruhollah why is this here


        //move and vision
        int maxIter = 0;
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                hero.setRecentPathForOpponent(new ArrayList<>()); // TODO this can go to the reset method
                maxIter = Math.max(maxIter, hero.getRecentPath().size());
            }
        }
        for (int stepNumber = 0; stepNumber < maxIter; stepNumber++) {
            moveHeroesOneStep(stepNumber); // TODO check move ap on prepare
            updateHeroVisions();
        }
        updateOpponentHeroRecentPaths();

        // end of move and vision

        //vision for players
        updatePlayerVisions();
        //end of vision for players
    }

    private void updatePlayerVisions() {
        for (Player player : players) {
            Set<Cell> vision = new HashSet<>();
            for (Hero hero : player.getHeroes()) {
                if (hero.getHp() == 0) {
                    continue;
                }
                vision.addAll(visionTools.getHeroVision(hero));
            }
            player.setVision(vision);
        }
    }

    private void updateOpponentHeroRecentPaths() {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                List<Cell> path = hero.getRecentPathForOpponent();
                List<Cell> ans = new ArrayList<>();
                if (path.size() > 0)
                    ans.add(path.get(0));
                for (int i = 1; i < path.size(); i++) {
                    if (path.get(i) != path.get(i - 1))
                        ans.add(path.get(i));
                }
                hero.setRecentPathForOpponent(ans);
            }
        }
    }

    private void updateHeroVisions() {
        for (Hero firstPlayerHero : players[0].getHeroes()) {
            for (Hero secondPlayerHero : players[1].getHeroes()) {
                if (visionTools.isInVision(firstPlayerHero.getCell(), secondPlayerHero.getCell())) {
                    firstPlayerHero.addToRecentPathForOpponent(firstPlayerHero.getCell());
                    secondPlayerHero.addToRecentPathForOpponent(secondPlayerHero.getCell());
                }
            }
        }
    }

    private void moveHeroesOneStep(int stepNumber) {
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                if (stepNumber < hero.getRecentPath().size()) {
                    hero.moveTo(hero.getRecentPath().get(stepNumber)); // at the end of iteration heroes are at their destination
                    if (stepNumber != 0)
                        player.setActionPoint(player.getActionPoint() - hero.getMoveApCost());
                }
            }
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

    private void updateHeroRecentPaths(List<HeroMovement> movements) {

        for (HeroMovement movement : movements)
        {
            Hero hero = movement.getHero();
            Ability ability = movement.getAbility();
            if (ability != null)
            {
                ability.setRemainingCoolDown(ability.getCoolDown());
            }
            List<Cell> recentPath = hero.getRecentPath();
//            Cell cell = hero.getCell();
//            recentPath.add(cell);
            recentPath.add(movement.getEndCell());
        }

//        for (Move move : moves) {
//            Hero hero = move.getHero();
//            List<Cell> recentPath = hero.getRecentPath();
//            Cell cell = hero.getCell();
//            recentPath.add(cell);
//            for (Direction direction : move.getMoves()) {
//                cell = nextCellIfNotWall(cell, direction); //it's valid
//                recentPath.add(cell);
//            }
//        }
    }

    private List<HeroMovement> preprocessMessageMoves(ClientTurnMessage message, Player player) {
        List<HeroMovement> heroMovements = getHeroMovements(message.getMoves(), player.getHeroes());
        List<HeroMovement> validMovements = MovementHandler.getValidMovements(heroMovements, map, player);

//        message.mergeMoves();   //filters moves (in new version)
//        List<Move> moves = message.getMoves();
//        for (Move move : moves) {
//            prepareMove(move);
//        }
        //        moves.sort(Comparator.comparingInt(o -> o.getMoves().size()));
//        postPrepare(moves);

        //setting ap for players
//        calculateMoveAp(moves, player);

        return fixMoveMessage(validMovements, player);
    }

    private List<HeroMovement> getHeroMovements(List<Move> moves, List<Hero> heroes)
    {
        List<HeroMovement> movements = new ArrayList<>();

        for (Move move : moves)
        {
            Hero hero = move.getHero();
            List<Direction> directions = move.getMoves();
            if (hero.getHp() == 0 || directions.size() < 1)
            {
                continue;
            }
            Cell targetCell = nextCellIfNotWall(hero.getCell(), directions.get(0));
            if (targetCell == null)
                continue;
            HeroMovement heroMovement = new HeroMovement(hero.getCell(), targetCell, hero);
            heroMovement.setAp(hero.getMoveApCost());
            movements.add(heroMovement);
        }

        for (Hero hero : heroes)
        {
            Cell currentCell = hero.getCell();
            if (currentCell == null)
            {
                continue;
            }
            HeroMovement noMove = new HeroMovement(currentCell, currentCell, hero);
            noMove.setAp(0);
            movements.add(noMove);
        }

        return movements;
    }

    private void calculateMoveAp(List<Move> moves, Player player) {
        for (Move move : moves) {
            player.setActionPoint(player.getActionPoint() - move.getGreedyApCost());
        }
    }

    private List<HeroMovement> fixMoveMessage(List<HeroMovement> heroMovements, Player player) {
        List<HeroMovement> finalMovements = new ArrayList<>();
        int ap = player.getActionPoint();

        for (HeroMovement movement : heroMovements)
        {
            int neededAP = movement.getAp();
            if (neededAP == 0)
                continue;
            ap -= neededAP;
            if (ap < 0)
                break;
            finalMovements.add(movement);
        }

        return finalMovements;
//        List<Move> newMoves = new ArrayList<>();
//        int ap = player.getActionPoint();
//        for (Move move : moves) {
//            ap -= move.getGreedyApCost();
//            if (ap < 0)
//                break;
//            newMoves.add(move);
//        }
//
//        return newMoves;
    }

    private void pick(ClientTurnMessage message1, ClientTurnMessage message2) { // TODO check this
        List<String> heroNames = new ArrayList<>(heroes.keySet());

        if (state.equals(GameState.PICK)) {
            if (message1.getType() == GameState.PICK && message2.getType() == GameState.PICK)
                doPickTurn(message1.getHeroName(), message2.getHeroName());
            else if (message1.getType() == GameState.PICK) {
                doPickTurn(message1.getHeroName(), heroNames.get(random.nextInt(heroes.size())));
            } else if (message2.getType() == GameState.PICK) {
                doPickTurn(heroNames.get(random.nextInt(heroes.size())), message2.getHeroName());
            } else {
                doPickTurn(heroNames.get(random.nextInt(heroes.size())), heroNames.get(random.nextInt(heroes.size())));
            }
            currentTurn.incrementAndGet();
        }
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

    private void cast(Cast cast, Player player, Map<Hero, Ability> fortifiedHeroes) {
        if (players[0] == player)
            cast(cast, 1, fortifiedHeroes);
        else
            cast(cast, 2, fortifiedHeroes);
    }

    private void cast(Cast cast, int player, Map<Hero, Ability> fortifiedHeroes) { // TODO this method needs serious cleaning
        Ability ability = cast.getAbility();
        AbilityType abilityType = ability.getType();
        Player player1 = players[player - 1];

        List<Hero> targetHeroes = new ArrayList<>();
        if (abilityType != AbilityType.DODGE) {
            targetHeroes = Arrays.asList(abilityTools.getAbilityTargets(ability, cast.getHero().getCell(),
                    map.getCell(cast.getTargetRow(), cast.getTargetColumn())));
        }
        addCastedAbility(cast, player, targetHeroes);

        for (Hero hero : targetHeroes) {
            switch (abilityType) {
                case DEFENSIVE:
                    if (player1.getHeroes().contains(hero)) {
                        int hp = Math.min(hero.getHp() + ability.getPower(), hero.getMaxHp());
                        hero.setHp(hp);
                    }
                    break;
                case OFFENSIVE:
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
            Cell cell = map.getCell(cast.getTargetRow(), cast.getTargetColumn());
            hero.moveTo(cell);
            hero.getRecentPath().add(cell);
        }
    }

    private void addCastedAbility(CastedAbility castedAbility, int playerNum) { // TODO what kind of a shit hell is this?

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

    private void addCastedAbility(Cast cast, int playerNum, List<Hero> targetHeroes) { // TODO what kind of a shit hell is this?
        CastedAbility castedAbility = new CastedAbility();
        castedAbility.setCasterHero(cast.getHero());
        castedAbility.setTargetHeroes(targetHeroes);
        castedAbility.setStartCell(cast.getHero().getCell());
        castedAbility.setAbility(cast.getAbility());
//        if (cast.getAbility().getType() == AbilityType.DODGE)
//        {
//            castedAbility.setEndCell();
//        } else
//        {
        if (cast.getAbility().getType() != AbilityType.DODGE) {
            castedAbility.setEndCell(abilityTools.getImpactCell(cast.getAbility(), cast.getHero().getCell(),
                    map.getCell(cast.getTargetRow(), cast.getTargetColumn())));
        } else {
            castedAbility.setEndCell(map.getCell(cast.getTargetRow(), cast.getTargetColumn()));
        }
//        }
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

    // deletes invalid moves
    private void prepareMove(Move move) {
        Cell cell = move.getHero().getCell();
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

    public void close()
    {
        graphicHandler.close();
        if (view)
        {
            viewer.close();
        }
    }
}
