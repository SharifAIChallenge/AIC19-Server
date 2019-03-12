package ir.sharif.aichallenge.server.thefinalbattle.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.engine.config.FileParam;
import ir.sharif.aichallenge.server.engine.config.StringParam;
import ir.sharif.aichallenge.server.engine.core.GameLogic;
import ir.sharif.aichallenge.server.thefinalbattle.model.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.ability.Ability;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.hero.ClientHero;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.hero.EmptyHero;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.AbilityType;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.Direction;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.GameState;
import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.GraphicHero;
import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.message.GraphicPickMessage;
import ir.sharif.aichallenge.server.thefinalbattle.model.message.InitialMessage;
import ir.sharif.aichallenge.server.thefinalbattle.model.message.PickMessage;
import ir.sharif.aichallenge.server.thefinalbattle.model.message.TurnMessage;
import ir.sharif.aichallenge.server.thefinalbattle.utils.VisionTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class GameHandler implements GameLogic {

    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");
    public static final StringParam PARAM_MAP_NAME = new StringParam("MapName", "");
    public static final StringParam CLIENT_ONE = new StringParam("TeamName0", "");
    public static final StringParam CLIENT_TWO = new StringParam("TeamName1", "");

    public static final int CLIENT_NUM = 2;
    public static final int CLIENT_RESPONSE_TIME = 850;
    public static final int CLIENT_FIRST_MOVE_RESPONSE_TIME = 1150;
    public static final int CLIENT_FIRST_TURN_RESPONSE_TIME = 5150;
    public static final int CLIENT_HERO_NUM = 4;
    private final int extraTime;
    private GameEngine gameEngine = new GameEngine();

    private InitialMessage initialMessage; // we need this field when we are sending it to the clients
    private InitialMessage graphicInitial;

    public GameHandler(AtomicInteger currentTurn, AtomicInteger currentMovePhase, boolean view, int extraTime) {
        gameEngine.setCurrentTurn(currentTurn);
        gameEngine.setCurrentMovePhase(currentMovePhase);
        gameEngine.setView(view);
        this.extraTime = extraTime;
    }

    @Override
    public int getClientsNum() {
        return CLIENT_NUM;
    }

    @Override
    public long getClientResponseTimeout() {
        int turn = gameEngine.getCurrentTurn().get();
        GameState state = gameEngine.getState();
        if (turn == 0 && state == GameState.INIT)
            return CLIENT_FIRST_TURN_RESPONSE_TIME + extraTime;
        if (state == GameState.MOVE && gameEngine.getCurrentMovePhase().get() == 0)
            return CLIENT_FIRST_MOVE_RESPONSE_TIME + extraTime;
        return CLIENT_RESPONSE_TIME + extraTime;
    }

    @Override
    public long getTurnTimeout() {
        return getClientResponseTimeout() + 200;
    }

    @Override
    public void init() {
        String initStr = readMapFile(PARAM_MAP);
        InitialMessage initialMessage = null;
        InitialMessage graphicInitial = null;
        try {
            JsonObject initJson = cleanCells(initStr);
            initialMessage = Json.GSON.fromJson(initJson, InitialMessage.class);
            graphicInitial = Json.GSON.fromJson(initJson, InitialMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Invalid map file!");
            System.exit(0);
        }
        gameEngine.initialize(initialMessage, PARAM_MAP_NAME.getValue(), CLIENT_ONE.getValue(),
                CLIENT_TWO.getValue());
        this.initialMessage = initialMessage;
        this.graphicInitial = graphicInitial;

        fixInitial(initialMessage);
        fixInitial(graphicInitial);

        fixGraphicHeroNames();

        gameEngine.getGraphicHandler().addInitMessage(graphicInitial);
    }

    private void fixGraphicHeroNames() {
        List<ClientHeroConstants> finalConstants = new ArrayList<>();
        List<ClientHeroConstants> heroConstants = graphicInitial.getHeroConstants();
        for (ClientHeroConstants heroConstant : heroConstants) {
            ClientHeroConstants constants = new ClientHeroConstants(heroConstant);
            ClientHeroConstants otherConstants = new ClientHeroConstants(constants);

            finalConstants.add(constants);
            finalConstants.add(otherConstants);
        }

        graphicInitial.setHeroConstants(finalConstants);
    }

    private void fixInitial(InitialMessage initialMessage) {
        initialMessage.getGameConstants().put("preprocessTimeout", CLIENT_FIRST_TURN_RESPONSE_TIME);
        initialMessage.getGameConstants().put("firstMoveTimeout", CLIENT_FIRST_MOVE_RESPONSE_TIME);
        initialMessage.getGameConstants().put("normalTimeout", CLIENT_RESPONSE_TIME);
        fixFortify(initialMessage);
    }

    private void fixFortify(InitialMessage initialMessage) {
        for (ClientAbilityConstants abilityConstants : initialMessage.getAbilityConstants()) {
            if (abilityConstants.getType() == AbilityType.FORTIFY) {
                abilityConstants.setType(AbilityType.DEFENSIVE);
            }
        }
    }

    private JsonObject cleanCells(String initStr) {
        JsonObject initJson = Json.GSON.fromJson(initStr, JsonObject.class);
        JsonObject map = initJson.getAsJsonObject("map");
        JsonArray oldCells = map.get("cells").getAsJsonArray();
        JsonArray newCells = new JsonArray();
        int rowNum = map.get("rowNum").getAsInt();
        int columnNum = map.get("columnNum").getAsInt();

        for (int i = 0; i < rowNum; i++) {
            JsonArray newRow = new JsonArray();

            for (int j = 0; j < columnNum; j++) {
                JsonObject oldCell = oldCells.get(i * columnNum + j).getAsJsonObject();
                newRow.add(oldCell);
            }

            newCells.add(newRow);
        }

        map.remove("cells");
        map.add("cells", newCells);
        initJson.remove("map");
        initJson.add("map", map);

        return initJson;
    }

    private String readMapFile(FileParam paramMap) {
        StringBuilder result = new StringBuilder();
        File mapFile = paramMap.getValue();
        if (mapFile == null || !mapFile.exists()) {
            System.err.println("Invalid map file!");
            System.exit(0);
        }
        try (Scanner in = new Scanner(mapFile)) {
            while (in.hasNext()) {
                result.append(in.nextLine());
                result.append("\n");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Map file not found!");
            System.exit(0);
        }

        return result.toString();
    }

    @Override
    public Message getUIInitialMessage() {
        return null;
    }//for ui

    @Override
    public Message[] getClientInitialMessages() {
        Message[] messages = new Message[2];

        for (int i = 0; i < CLIENT_NUM; i++) {
            JsonArray clientInitialJsonArray = new JsonArray();
            JsonObject initialJsonObject = Json.GSON.toJsonTree(initialMessage).getAsJsonObject();
            initialJsonObject.remove("map");
            JsonObject mapJsonObject = gameEngine.getMap().getClientInitialMap(i);
            initialJsonObject.add("map", mapJsonObject);

            clientInitialJsonArray.add(initialJsonObject);
            messages[i] = new Message(Message.NAME_INIT, clientInitialJsonArray);
        }
        return messages;
    }//for clients

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {
        if (gameEngine.getState() == GameState.INIT) {
            gameEngine.setState(GameState.PICK);
            return;
        }
        Event[] playerOneEvents = clientsEvent[0];
        Event[] playerTwoEvents = clientsEvent[1];
        ClientTurnMessage message1 = prepareClientMessage(playerOneEvents, 0);
        ClientTurnMessage message2 = prepareClientMessage(playerTwoEvents, 1);
        gameEngine.doTurn(message1, message2);
    }

    private ClientTurnMessage prepareClientMessage(Event[] events, int player) {
        ClientTurnMessage message = new ClientTurnMessage();
        for (Event event : events) {
            try {
                switch (event.getType()) {
                    case "cast":
                        Cast cast = prepareCast(player, event);
                        if (cast == null) {
                            continue;
                        }
                        message.getCasts().add(cast);
                        message.setType(GameState.ACTION);
                        break;
                    case "move":
                        Move move = prepareMove(player, event);
                        if (move == null || move.getMoves().size() == 0) {
                            continue;
                        }
                        message.getMoves().add(move);
                        message.setType(GameState.MOVE);

                        break;
                    case "pick":
                        String heroName = event.getArgs()[0];
                        Set<String> heroes = gameEngine.getHeroes().keySet();
                        if (!heroes.contains(heroName))
                            break;
                        message.setHeroName(heroName);
                        message.setType(GameState.PICK);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    private Move prepareMove(int player, Event event) {
        Hero hero;
        int heroId;
        try {
            heroId = Integer.parseInt(event.getArgs()[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        String onlyMove;                 //because of new changes in game
        try {
            onlyMove = Json.GSON.fromJson(event.getArgs()[1], String.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }

        String[] moves = new String[1];
        moves[0] = onlyMove;                 //because of new changes in game

        hero = gameEngine.getPlayers()[player].getHero(heroId);
        if (hero == null)
            return null;
        List<Direction> directions = new ArrayList<>();
        for (String move : moves) {
            switch (move) {
                case "UP":
                    directions.add(Direction.UP);
                    break;
                case "DOWN":
                    directions.add(Direction.DOWN);
                    break;
                case "RIGHT":
                    directions.add(Direction.RIGHT);
                    break;
                case "LEFT":
                    directions.add(Direction.LEFT);
                    break;
            }
        }
        return new Move(directions, hero);
    }

    private Cast prepareCast(int playerNum, Event event) {
        Player player = gameEngine.getPlayers()[playerNum];
        int heroId;
        try {
            heroId = Integer.parseInt(event.getArgs()[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        Hero hero = player.getHero(heroId);
        if (hero == null)
            return null;

        String abilityName = event.getArgs()[1];
        Ability ability = hero.getAbility(abilityName);
        if (ability == null || ability.getRemainingCoolDown() > 0)
            return null;

        int targetRow;
        int targetColumn;
        try {
            targetRow = Integer.parseInt(event.getArgs()[2]);
            targetColumn = Integer.parseInt(event.getArgs()[3]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        if (!gameEngine.getMap().isInMap(targetRow, targetColumn))
            return null;

        if (ability.getType() == AbilityType.DODGE) {
            Cell cell = fixDodgeTarget(hero, gameEngine.getMap().getCell(targetRow, targetColumn), ability.getRange(),
                    ability.isLobbing());
            if (cell == null/* || cell.equals(hero.getCell())*/) {
                return null;
            }
            return new Cast(hero, ability, cell.getRow(), cell.getColumn());
        }

        return new Cast(hero, ability, targetRow, targetColumn);
    }

    private Cell fixDodgeTarget(Hero hero, Cell targetCell, int range, boolean isLobbing) // TODO check this
    {
        if (hero.getCell() == null)
            return null;

        VisionTools visionTools = gameEngine.getVisionTools();
        Cell[] rayCells = visionTools.getRayCells(hero.getCell(), targetCell, isLobbing);

        for (int i = rayCells.length - 1; i >= 0; i--) {
            if (visionTools.manhattanDistance(hero.getCell(), rayCells[i]) <= range/*&& !rayCells[i].isWall()*/) {
                return rayCells[i];
            }
        }
        return null;
    }

    @Override
    public void generateOutputs() {
        // statistical logs
        StringBuilder context = new StringBuilder();
        context.append("[");
        for (int i = 0; i < gameEngine.getServerViewJsons().size(); i++) {
            context.append(Json.GSON.toJson(gameEngine.getServerViewJsons().get(i)));
            if (i == gameEngine.getServerViewJsons().size() - 1)
                break;
            context.append(",\n");
        }
        context.append("]");

        try {
            Files.write(Paths.get("server_view.log"), context.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Message getUIMessage() {
        GameState state = gameEngine.getState();
        switch (state) {
            case PICK:
                return getUIPickMessage();
            case MOVE:
                return getUIMoveMessage();
            case ACTION:
                return getUIActionMessage();
        }
        return null;
    }

    private Message getUIPickMessage() {
        GraphicPickMessage graphicPickMessage = new GraphicPickMessage();
        GraphicHero[][] heroes = new GraphicHero[CLIENT_NUM][CLIENT_HERO_NUM];
        for (int i = 0; i < CLIENT_NUM; i++) {
            for (int j = 0; j < CLIENT_HERO_NUM; j++) {
                Hero hero = gameEngine.getPlayers()[i].getHeroes().get(j);
                heroes[i][j] = new GraphicHero(hero.getId(), hero.getName(),
                        hero.getCell().getRow(), hero.getCell().getColumn());
            }
        }
        graphicPickMessage.setHeroes(heroes);

        GraphicPickMessage[] graphicPickMessages = new GraphicPickMessage[1];
        graphicPickMessages[0] = graphicPickMessage;
        return new Message(Message.NAME_PICK, Json.GSON.toJsonTree(graphicPickMessages).getAsJsonArray());
    }

    private Message getUIMoveMessage() {
        return null;
    }

    private Message getUIActionMessage() {
        return null;
    }

    @Override
    public Message getStatusMessage() {
        return null;
    }

    @Override
    public Message[] getClientMessages() {
        if (gameEngine.getState() == GameState.INIT) {
            return getClientInitialMessages();
        }

        Player[] players = gameEngine.getPlayers();
        Message[] messages = new Message[2];
        GameState state = gameEngine.getState();

        switch (state) {
            case PICK:
                setMessagesForPick(players, messages);
                break;
            case MOVE:
            case ACTION:
                setMessagesForTurn(players, messages);
                break;
        }
        return messages;
    }

    private void setMessagesForPick(Player[] players, Message[] messages) {
        for (int i = 0; i < CLIENT_NUM; i++) {
            PickMessage pickMessage = new PickMessage();
            List<EmptyHero> myHeroes = new ArrayList<>();
            for (Hero hero : players[i].getHeroes()) {
                EmptyHero emptyHero = new EmptyHero(hero.getId(), hero.getName());
                myHeroes.add(emptyHero);
            }
            pickMessage.setMyHeroes(myHeroes);
            List<EmptyHero> oppHeroes = new ArrayList<>();
            for (Hero hero : players[1 - i].getHeroes()) {
                EmptyHero emptyHero = new EmptyHero(hero.getId(), hero.getName());
                oppHeroes.add(emptyHero);
            }
            pickMessage.setMyHeroes(myHeroes);
            pickMessage.setOppHeroes(oppHeroes);
            pickMessage.setCurrentTurn(gameEngine.getCurrentTurn().get());
            //make json array and message[i]
            PickMessage[] pickMessages = new PickMessage[1];
            pickMessages[0] = pickMessage;

            JsonArray messageArray = Json.GSON.toJsonTree(pickMessages).getAsJsonArray();
            messages[i] = new Message(Message.NAME_PICK, messageArray);
        }
    }

    private void setMessagesForTurn(Player[] players, Message[] messages) {
        for (int i = 0; i < CLIENT_NUM; i++) {
            Player player = players[i];
            TurnMessage turnMessage = new TurnMessage();
            turnMessage.setMyScore(player.getScore());
            turnMessage.setOppScore(player.getOpponent().getScore()); // client_num must be 2
            turnMessage.setCurrentPhase(gameEngine.getState().name());
            turnMessage.setMovePhaseNum(gameEngine.getState() == GameState.MOVE ?
                    gameEngine.getCurrentMovePhase().get() : -1);
            turnMessage.setCurrentTurn(gameEngine.getCurrentTurn().get());
            turnMessage.setAP(player.getActionPoint());
            turnMessage.setMap(getClientMap(player).getCells());
            turnMessage.setMyHeroes(getClientHeroes(player));
            turnMessage.setOppHeroes(getClientOppHeroes(player));
            turnMessage.setMyCastAbilities(player.getMyCastedAbilities());
            turnMessage.setOppCastAbilities(player.getOppCastedAbilities());
            turnMessage.setMaxOvertime(gameEngine.getMaxOvertime());
            turnMessage.setRemainingOvertime(gameEngine.getRemainingOvertime());
            // make json array and message[i]
            TurnMessage[] turnMessages = new TurnMessage[1];
            turnMessages[0] = turnMessage;
            messages[i] = new Message(Message.NAME_TURN, Json.GSON.toJsonTree(turnMessages).getAsJsonArray());
        }
    }

    private List<ClientHero> getClientHeroes(Player player) {
        List<ClientHero> clientHeroes = new ArrayList<>();
        for (Hero hero : player.getHeroes()) {
            clientHeroes.add(hero.getClientHero());
        }
        return clientHeroes;
    }

    private List<ClientHero> getClientOppHeroes(Player player) {
        Player opponent = player.getOpponent();
        List<ClientHero> clientHeroes = new ArrayList<>();
        for (Hero hero : opponent.getHeroes()) {
            boolean isInVision = player.getVision().contains(hero.getCell());
            ClientHero clientHero = new ClientHero();
            clientHero.setId(hero.getId());
            clientHero.setType(hero.getName());
            if (hero.getCell() == null)
                clientHero.setCurrentHP(0);     //dead
            else if (!isInVision)
                clientHero.setCurrentHP(-1);    //not in vision
            else
                clientHero.setCurrentHP(hero.getHp());  //in vision

            clientHero.setCurrentCell(isInVision ? new EmptyCell(hero.getCell().getRow(), hero.getCell().getColumn())
                    : null);
            //recent path
            List<EmptyCell> recentPathList = new ArrayList<>();
            for (Cell cell : hero.getRecentPathForOpponent()) {
                EmptyCell emptyCell = new EmptyCell(cell.getRow(), cell.getColumn());
                recentPathList.add(emptyCell);
            }
            EmptyCell[] recentPath = new EmptyCell[recentPathList.size()];
            recentPath = recentPathList.toArray(recentPath);
            clientHero.setRecentPath(recentPath);
            clientHero.setRespawnTime(hero.getRespawnTime());
            clientHeroes.add(clientHero);
        }
        return clientHeroes;
    }

    private ClientMap getClientMap(Player player) {
        Map map = gameEngine.getMap();
        VisionTools visionTools = new VisionTools(map);
        List<Hero> heroes = player.getHeroes();
        ClientCell[][] clientCells = new ClientCell[map.getNumberOfRows()][map.getNumberOfColumns()];
        for (int i = 0; i < map.getNumberOfRows(); i++) {
            for (int j = 0; j < map.getNumberOfColumns(); j++) {
                Cell cell = map.getCell(i, j);
                ClientCell clientCell = new ClientCell();
                clientCell.setWall(cell.isWall());
                clientCell.setInMyRespawnZone(player.getRespawnZone().contains(cell));
                clientCell.setInOppRespawnZone(player.getOpponent().getRespawnZone().contains(cell));
                clientCell.setInObjectiveZone(map.getObjectiveZone().contains(cell));
                //vision
                clientCell.setInVision(false);
                for (Hero hero : heroes) {
                    Cell heroCell = hero.getCell();
                    if (heroCell != null && visionTools.isInVision(heroCell, cell))
                        clientCell.setInVision(true);
                }
                //end of vision
                clientCell.setRow(i);
                clientCell.setColumn(j);

                clientCells[i][j] = clientCell;
            }
        }
        return new ClientMap(clientCells);
    }

    @Override
    public Event[] makeEnvironmentEvents() {
        return new Event[0];
    }

    @Override
    public boolean isGameFinished() {
        return gameEngine.getMaxOvertime() == 0 || gameEngine.getRemainingOvertime() == 0;
    }

    @Override
    public void terminate() {
        gameEngine.getGraphicHandler().addEndMessage();
        gameEngine.close();
    }
}
