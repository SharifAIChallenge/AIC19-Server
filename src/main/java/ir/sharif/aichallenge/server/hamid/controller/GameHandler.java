package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.engine.core.GameLogic;
import ir.sharif.aichallenge.server.hamid.model.*;
import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.client.CastedAbility;
import ir.sharif.aichallenge.server.hamid.model.client.ClientCell;
import ir.sharif.aichallenge.server.hamid.model.client.ClientMap;
import ir.sharif.aichallenge.server.hamid.model.client.EmptyCell;
import ir.sharif.aichallenge.server.hamid.model.client.hero.ClientHero;
import ir.sharif.aichallenge.server.hamid.model.client.hero.Cooldown;
import ir.sharif.aichallenge.server.hamid.model.client.hero.EmptyHero;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;
import ir.sharif.aichallenge.server.hamid.model.message.PickMessage;
import ir.sharif.aichallenge.server.hamid.model.message.TurnMessage;
import ir.sharif.aichallenge.server.hamid.utils.Pair;
import ir.sharif.aichallenge.server.hamid.utils.VisionTools;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
@Log4j
public class GameHandler implements GameLogic {

    public static final int CLIENT_NUM = 2;
    public static final int CLIENT_RESPONSE_TIME = 0;
    public static final int TURN_TIMEOUT = 0;
    private GameEngine gameEngine = new GameEngine();
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
        //todo read from map
        //todo initialize game
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
        //todo handle turn
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
            turnMessage.setCastAbilities(gameEngine.getCastingAbilities());
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
        Player opponent = players[1-i];
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
                }
                else {
                    clientCell.setInMyRespawnZone(map.getPlayer2RespawnZone().contains(cell));
                    clientCell.setInOppRespawnZone(map.getPlayer1RespawnZone().contains(cell));
                }
                clientCell.setInObjectZone(map.getObjectiveZone().contains(cell));
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
