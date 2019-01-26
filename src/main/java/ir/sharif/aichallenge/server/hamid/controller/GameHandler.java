package ir.sharif.aichallenge.server.hamid.controller;

import com.google.gson.JsonArray;
import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.engine.core.GameLogic;
import ir.sharif.aichallenge.server.hamid.model.Cell;
import ir.sharif.aichallenge.server.hamid.model.Hero;
import ir.sharif.aichallenge.server.hamid.model.Map;
import ir.sharif.aichallenge.server.hamid.model.Player;
import ir.sharif.aichallenge.server.hamid.model.client.ClientCell;
import ir.sharif.aichallenge.server.hamid.model.client.ClientMap;
import ir.sharif.aichallenge.server.hamid.model.message.TurnMessage;
import ir.sharif.aichallenge.server.hamid.utils.VisionTools;
import lombok.extern.log4j.Log4j;

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

        for (int i = 0; i < CLIENT_NUM; i++) {
            TurnMessage turnMessage = new TurnMessage();
            Player player = players[i];
            turnMessage.setMyScore(player.getScore());
            turnMessage.setOppScore(players[1 - i].getScore()); // client_num must be 2
            turnMessage.setCurrentPhase(gameEngine.getState().name());
            turnMessage.setCurrentTurn(gameEngine.getCurrentTrun().get());
            turnMessage.setMap(getClientMap(i));
            turnMessage.setMyHeroes(player.getHeroes());
            turnMessage.setOppHeroes(players[1 - i].getHeroes()); // client_num must be 2
            //todo broken walls and created walls
            //todo make json array and message[i]
        }
        return messages;
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
