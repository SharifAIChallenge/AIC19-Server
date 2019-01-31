package ir.sharif.aichallenge.server.hamid.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.engine.config.FileParam;
import ir.sharif.aichallenge.server.engine.core.GameLogic;
import ir.sharif.aichallenge.server.hamid.model.*;
import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.client.ClientCell;
import ir.sharif.aichallenge.server.hamid.model.client.ClientMap;
import ir.sharif.aichallenge.server.hamid.model.client.EmptyCell;
import ir.sharif.aichallenge.server.hamid.model.client.hero.ClientHero;
import ir.sharif.aichallenge.server.hamid.model.client.hero.Cooldown;
import ir.sharif.aichallenge.server.hamid.model.client.hero.EmptyHero;
import ir.sharif.aichallenge.server.hamid.model.enums.Direction;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;
import ir.sharif.aichallenge.server.hamid.model.graphic.GraphicHero;
import ir.sharif.aichallenge.server.hamid.model.graphic.RemainingCooldown;
import ir.sharif.aichallenge.server.hamid.model.graphic.StatusHero;
import ir.sharif.aichallenge.server.hamid.model.graphic.message.GraphicPickMessage;
import ir.sharif.aichallenge.server.hamid.model.graphic.message.StatusMessage;
import ir.sharif.aichallenge.server.hamid.model.message.InitialMessage;
import ir.sharif.aichallenge.server.hamid.model.message.PickMessage;
import ir.sharif.aichallenge.server.hamid.model.message.TurnMessage;
import ir.sharif.aichallenge.server.hamid.utils.VisionTools;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Log4j
public class GameHandler implements GameLogic {

    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");

    public static final int CLIENT_NUM = 2;
    public static final int CLIENT_RESPONSE_TIME = 0;
    public static int TURN_TIMEOUT = 0;
    public static int PICK_TURN_TIMEOUT = 0;
    public static final int CLIENT_HERO_NUM = 4;
    private GameEngine gameEngine = new GameEngine();
    private Gson gson = new Gson();

    private InitialMessage initialMessage; // we need this field when we are sending it to the clients

    @Override
    public int getClientsNum() {
        return CLIENT_NUM;
    }

    @Override
    public long getClientResponseTimeout() {
        return CLIENT_RESPONSE_TIME;
    }

    @Override
    public long getTurnTimeout() {
        if (gameEngine.getState() == GameState.PICK)
            return PICK_TURN_TIMEOUT;
        return TURN_TIMEOUT;
    }

    @Override
    public void init() {
        String initStr = readMapFile(PARAM_MAP);
        InitialMessage initialMessage = null;
        try {
            initialMessage = Json.GSON.fromJson(initStr, InitialMessage[].class)[0];
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid map file!");
            System.exit(0);
        }
        gameEngine.initialize(initialMessage);
        this.initialMessage = initialMessage;
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

        for (int i = 0; i < CLIENT_NUM; i++)
        {
            JsonArray clientInitialJsonArray = new JsonArray();
            JsonObject initialJsonObject = Json.GSON.toJsonTree(initialMessage).getAsJsonObject();
            initialJsonObject.remove("map");
            JsonArray mapJsonArray = gameEngine.getMap().getClientInitialMap(i);
            initialJsonObject.add("map", mapJsonArray);
            clientInitialJsonArray.add(initialJsonObject);
            messages[i] = new Message(Message.NAME_INIT, clientInitialJsonArray);
        }
        return messages;
    }//for clients

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {
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
                        if (cast == null)
                        {
                            continue;
                        }
                        message.getCasts().add(cast);
                        message.setType(GameState.CAST);
                        break;
                    case "move":
                        Move move = prepareMove(player, event);
                        if (move == null)
                        {
                            continue;
                        }
                        message.getMoves().add(move);
                        message.setType(GameState.MOVE);

                        break;
                    case "pick": // TODO check this
                        String heroName = event.getArgs()[0];
                        message.setHeroName(heroName);
                        message.setType(GameState.PICK);
                        break;
                }
            } catch (Exception ignore) {

            }
        }
        return message;
    }

    private Move prepareMove(int player, Event event) {
        Hero hero;
        int heroId = Integer.parseInt(event.getArgs()[0]);
        String list = event.getArgs()[1];

        hero = gameEngine.getPlayers()[player].getHero(heroId);
        if (hero == null)
            return null;
        List<Direction> directions = new ArrayList<>();
        String[] moves = list.split(",");
        for (String move : moves) {
            if (move.contains("UP")) {
                directions.add(Direction.UP);
            } else if (move.contains("DOWN")) {
                directions.add(Direction.DOWN);
            } else if (move.contains("RIGHT")) {
                directions.add(Direction.RIGHT);
            } else if (move.contains("LEFT")) {
                directions.add(Direction.LEFT);
            }
        }
        return new Move(directions, hero);
    }

    private Cast prepareCast(int playerNum, Event event) {
        Player player = gameEngine.getPlayers()[playerNum];
        int heroId = Integer.parseInt(event.getArgs()[0]);
        Hero hero = player.getHero(heroId);
        if (hero == null)
            return null;

        String abilityName = event.getArgs()[0];
        Ability ability = hero.getAbility(abilityName);

        int targetRow = Integer.parseInt(event.getArgs()[2]);
        int targetColumn = Integer.parseInt(event.getArgs()[3]);

        return new Cast(hero, ability, targetRow, targetColumn);
    }

    @Override
    public void generateOutputs() {
        // statistical logs
    }

    @Override
    public Message getUIMessage() {
        GameState state = gameEngine.getState();
        switch (state) {
            case PICK:
                return getUIPickMessage();
                break;
            case MOVE:
                return getUIMoveMessage();
                break;
            case CAST:
                return getUIActionMessage();
                break;
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
                        hero.getCell().getRow(), hero.getCell().getColumn());   //todo respawn heroes just after last pick
            }
        }
        graphicPickMessage.setHeroes(heroes);

        GraphicPickMessage[] graphicPickMessages = new GraphicPickMessage[1];
        graphicPickMessages[0] = graphicPickMessage;
        return new Message(Message.NAME_PICK, Json.GSON.toJsonTree(graphicPickMessages).getAsJsonArray());
    }

    private Message getUIMoveMessage() {

    }

    @Override
    public Message getStatusMessage() {
        StatusMessage statusMessage = new StatusMessage();
        StatusHero[][] heroes = new StatusHero[CLIENT_NUM][CLIENT_HERO_NUM];
        for (int i = 0; i < CLIENT_NUM; i++) {
            for (int j = 0; j < CLIENT_HERO_NUM; j++) {
                Hero hero = gameEngine.getPlayers()[i].getHeroes().get(j);
                StatusHero statusHero = new StatusHero();
                statusHero.setId(hero.getId());
                statusHero.setCurrentHP(hero.getHp());
                List<RemainingCooldown> remainingCooldowns = new ArrayList<>();
                for (Ability ability : hero.getAbilities()) {
                    remainingCooldowns.add(new RemainingCooldown(ability.getName(), ability.getRemainingCoolDown()));
                }
                statusHero.setRemainingCooldowns(remainingCooldowns);
                heroes[i][j] = statusHero;
            }
        }
        statusMessage.setHeroes(heroes);

        StatusMessage[] statusMessages = new StatusMessage[1];
        statusMessages[0] = statusMessage;
        return new Message(Message.NAME_STATUS, Json.GSON.toJsonTree(statusMessages).getAsJsonArray());
    }

    @Override
    public Message[] getClientMessages() {
        Player[] players = gameEngine.getPlayers();
        Message[] messages = new Message[2];
        GameState state = gameEngine.getState();

        switch (state) {
            case PICK:
                setMessagesForPick(players, messages);
                break;
            case MOVE:
            case CAST:
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
            pickMessage.setMyHeroes(oppHeroes);
            pickMessage.setCurrentTurn(gameEngine.getCurrentTurn().get());
            //make json array and message[i]
            PickMessage[] pickMessages = new PickMessage[1];
            pickMessages[0] = pickMessage;
            messages[i] = new Message(Message.NAME_PICK, Json.GSON.toJsonTree(pickMessages).getAsJsonArray());
        }
    }

    private void setMessagesForTurn(Player[] players, Message[] messages) {
        for (int i = 0; i < CLIENT_NUM; i++) {
            TurnMessage turnMessage = new TurnMessage();
            Player player = players[i];
            turnMessage.setMyScore(player.getScore());
            turnMessage.setOppScore(players[1 - i].getScore()); // client_num must be 2
            turnMessage.setCurrentPhase(gameEngine.getState().name());
            turnMessage.setCurrentTurn(gameEngine.getCurrentTurn().get());
            turnMessage.setMap(getClientMap(i));
            turnMessage.setMyHeroes(getClientHeroes(i));
            turnMessage.setOppHeroes(getClientOppHeroes(i));
            if (i == 0) {
                turnMessage.setMyCastedAbilities(gameEngine.getPlayer1castedAbilities());
                turnMessage.setOppCastedAbilities(gameEngine.getPlayer1oppCastedAbilities());
            } else {
                turnMessage.setMyCastedAbilities(gameEngine.getPlayer2castedAbilities());
                turnMessage.setOppCastedAbilities(gameEngine.getPlayer2oppCastedAbilities());
            }
            //make json array and message[i]
            TurnMessage[] turnMessages = new TurnMessage[1];
            turnMessages[0] = turnMessage;
            messages[i] = new Message(Message.NAME_TURN, Json.GSON.toJsonTree(turnMessages).getAsJsonArray());
        }
    }

    private List<ClientHero> getClientHeroes(int i) {
        Player[] players = gameEngine.getPlayers();
        List<ClientHero> clientHeroes = new ArrayList<>();
        for (Hero hero : players[i].getHeroes()) {
            ClientHero clientHero = new ClientHero();
            clientHero.setId(hero.getId());
            clientHero.setType(hero.getName());
            clientHero.setCurrentHP(hero.getHp());
            //cooldowns
            List<Cooldown> cooldowns = new ArrayList<>();
            for (Ability ability : hero.getAbilities()) {
                Cooldown cooldown = new Cooldown(ability.getName(), ability.getRemainingCoolDown());
                cooldowns.add(cooldown);
            }
            Cooldown[] cool = new Cooldown[cooldowns.size()];
            cool = cooldowns.toArray(cool);
            clientHero.setCooldowns(cool);  //end of cooldowns
            clientHero.setCurrentCell(new EmptyCell(hero.getCell().getRow(), hero.getCell().getColumn()));
            //recent path
            List<EmptyCell> recentPathList = new ArrayList<>();
            for (Cell cell : hero.getRecentPath()) {
                EmptyCell emptyCell = new EmptyCell(cell.getRow(), cell.getColumn());
                recentPathList.add(emptyCell);
            }
            EmptyCell[] recentPath = new EmptyCell[recentPathList.size()];
            recentPath = recentPathList.toArray(recentPath);
            clientHero.setRecentPath(recentPath);
            clientHero.setRespawnTime(hero.getMaxRespawnTime());
            clientHeroes.add(clientHero);
        }
        return clientHeroes;
    }

    private List<ClientHero> getClientOppHeroes(int i) {
        Player[] players = gameEngine.getPlayers();
        Player opponent = players[1 - i];
        List<ClientHero> clientHeroes = new ArrayList<>();
        for (Hero hero : opponent.getHeroes()) {
            ClientHero clientHero = new ClientHero();
            clientHero.setId(hero.getId());
            clientHero.setType(hero.getName());
            if (hero.getCell() == null)
                clientHero.setCurrentHP(0);     //dead
            else if (players[i].getVision().contains(hero.getCell()))
                clientHero.setCurrentHP(-1);    //not in vision
            else
                clientHero.setCurrentHP(hero.getHp());  //in vision
            //cooldowns
            List<Cooldown> cooldowns = new ArrayList<>();
            for (Ability ability : hero.getAbilities()) {
                Cooldown cooldown = new Cooldown(ability.getName(), ability.getRemainingCoolDown());
                cooldowns.add(cooldown);
            }
            Cooldown[] cool = new Cooldown[cooldowns.size()];
            cool = cooldowns.toArray(cool);
            clientHero.setCooldowns(cool);  //end of cooldowns
            clientHero.setCurrentCell(players[i].getVision().contains(hero.getCell()) ?
                    new EmptyCell(hero.getCell().getRow(), hero.getCell().getColumn()) : null);
            //recent path
            List<EmptyCell> recentPathList = new ArrayList<>();
            for (Cell cell : hero.getRecentPathForOpponent()) {
                EmptyCell emptyCell = new EmptyCell(cell.getRow(), cell.getColumn());
                recentPathList.add(emptyCell);
            }
            EmptyCell[] recentPath = new EmptyCell[recentPathList.size()];
            recentPath = recentPathList.toArray(recentPath);
            clientHero.setRecentPath(recentPath);
            clientHero.setRespawnTime(hero.getMaxRespawnTime());
            clientHeroes.add(clientHero);
        }
        return clientHeroes;
    }

    private ClientMap getClientMap(int playerNum) {
        Map map = gameEngine.getMap();
        VisionTools visionTools = new VisionTools(map);
        Player[] players = gameEngine.getPlayers();
        List<Hero> heroes = players[playerNum].getHeroes();
        ClientCell[][] clientCells = new ClientCell[map.getNumberOfRows()][map.getNumberOfColumns()];
        for (int i = 0; i < map.getNumberOfRows(); i++) {
            for (int j = 0; j < map.getNumberOfColumns(); j++) {
                Cell cell = map.getCell(i, j);
                ClientCell clientCell = new ClientCell();
                clientCell.setWall(cell.isWall());
                if (playerNum == 0) {
                    clientCell.setInMyRespawnZone(map.getPlayer1RespawnZone().contains(cell));
                    clientCell.setInOppRespawnZone(map.getPlayer2RespawnZone().contains(cell));
                } else {
                    clientCell.setInMyRespawnZone(map.getPlayer2RespawnZone().contains(cell));
                    clientCell.setInOppRespawnZone(map.getPlayer1RespawnZone().contains(cell));
                }
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
        Player[] players = gameEngine.getPlayers();
        int maxScore = gameEngine.getMaxScore();
        int maxTurns = gameEngine.getMaxTurns();

        return gameEngine.getCurrentTurn().get() >= maxTurns || players[0].getScore() >= maxScore ||
                players[1].getScore() >= maxScore;
    }

    @Override
    public void terminate() {

    }
}
