package ir.sharif.aichallenge.server.hamid.controller;

import com.google.gson.Gson;
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
import java.util.concurrent.atomic.AtomicInteger;

@Log4j
public class GameHandler implements GameLogic {

    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");

    public static final int CLIENT_NUM = 2;
    public static final int CLIENT_RESPONSE_TIME = 0;
    public static final int TURN_TIMEOUT = 0;
    private GameEngine gameEngine = new GameEngine();
    private Gson gson = new Gson();
    private AtomicInteger currentTrun;

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
        //todo check it pick or else
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
            e.printStackTrace();
        }

        return result.toString();
    }

    @Override
    public Message getUIInitialMessage() {
        return null;
    }//for ui

    @Override
    public Message[] getClientInitialMessages() {
        //todo prepare map
        return new Message[0];
    }//for clients

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {
        Event[] playerOneEvents = clientsEvent[0];
        Event[] playerTwoEvents = clientsEvent[1];

        ClientTurnMessage message1 = new ClientTurnMessage();
        ClientTurnMessage message2 = new ClientTurnMessage();

        prepareClientMessage(playerOneEvents, message1, 0);
        prepareClientMessage(playerTwoEvents, message2, 1);

        gameEngine.doTurn(message1 , message2);

    }

    private void prepareClientMessage(Event[] events, ClientTurnMessage message, int player) {
        for (Event event : events) {
            try {
                switch (event.getType()) {
                    case "cast":
                        Cast cast = prepareCast(message, player, event);
                        message.getCasts().add(cast);
                        message.setType(GameState.CAST);
                        break;
                    case "move":
                        Move move = prepareMove(player, event);
                        message.getMoves().add(move);
                        message.setType(GameState.MOVE);

                        break;
                    case "pick":
                        int heroId = Integer.parseInt(event.getArgs()[0]);
                        message.setHeroId(heroId);
                        message.setType(GameState.PICK);
                        break;
                }
            } catch (Exception ignore) {

            }
        }
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

    private Cast prepareCast(ClientTurnMessage message1, int player, Event event) {
        int heroId = Integer.parseInt(event.getArgs()[0]);
        String abilityName = event.getArgs()[0];
        int targetRow = Integer.parseInt(event.getArgs()[2]);
        int targetCollumn = Integer.parseInt(event.getArgs()[3]);
        Hero hero = gameEngine.getPlayers()[player].getHero(heroId);
        if (hero == null)
            return null;
        Cast cast = new Cast(hero, hero.getAbility(abilityName), targetRow, targetCollumn);
        return cast;
    }

    @Override
    public void generateOutputs() {
    }

    @Override
    public Message getUIMessage() {
        return null;
    }

    @Override
    public Message getStatusMessage() {
        return null;
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
            pickMessage.setCurrentTurn(gameEngine.getCurrentTurn().get());  //todo correct?
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
            clientHero.setRespawnTime(hero.getRespawnTime());
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
            clientHero.setRespawnTime(hero.getRespawnTime());
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
        //todo check number if turns
        return false;
    }

    @Override
    public void terminate() {

    }
}
