package ir.sharif.aichallenge.server.thefinalbattle.controller;

import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.thefinalbattle.model.CastedAbility;
import ir.sharif.aichallenge.server.thefinalbattle.model.Cell;
import ir.sharif.aichallenge.server.thefinalbattle.model.Hero;
import ir.sharif.aichallenge.server.thefinalbattle.model.Player;
import ir.sharif.aichallenge.server.thefinalbattle.model.ability.Ability;
import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.message.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.message.InitialMessage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static ir.sharif.aichallenge.server.thefinalbattle.controller.GameHandler.CLIENT_HERO_NUM;
import static ir.sharif.aichallenge.server.thefinalbattle.controller.GameHandler.CLIENT_NUM;

public class GraphicHandler {
    private static final String LOG_FILE = "graphic.log";
    private RandomAccessFile logFile;

    private GameEngine gameEngine;

    public GraphicHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void addInitMessage(InitialMessage initialMessage) {
        InitialMessage[] initialMessages = new InitialMessage[1];
        initialMessages[0] = initialMessage;
        Message message = new Message(Message.NAME_INIT, initialMessages);
        try {
            logFile = new RandomAccessFile(LOG_FILE, "rw");
            logFile.setLength(0);
            String logStr = "[" + Json.GSON.toJson(message) + "]";
            logFile.write(logStr.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPickMessage() {
        GraphicPickMessage graphicPickMessage = getGraphicPickMessage(true);

        GraphicPickMessage[] graphicPickMessages = new GraphicPickMessage[1];
        graphicPickMessages[0] = graphicPickMessage;
        Message message = new Message(Message.NAME_PICK, graphicPickMessages);
        addMessageToLog(message);
    }

    public GraphicPickMessage getGraphicPickMessage(boolean isGraphic) {
        GraphicPickMessage graphicPickMessage = new GraphicPickMessage();
        GraphicHero[][] graphicHeroes = new GraphicHero[CLIENT_NUM][CLIENT_HERO_NUM];
        for (int i = 0; i < CLIENT_NUM; i++) {
            for (int j = 0; j < CLIENT_HERO_NUM; j++) {
                Hero hero = gameEngine.getPlayers()[i].getHeroes().get(j);
                graphicHeroes[i][j] = hero.getGraphicHero(i, isGraphic);
            }
        }
        graphicPickMessage.setHeroes(graphicHeroes);

        return graphicPickMessage;
    }

    public void addMoveMessage() {
        List<String> allMovements = getAllMovements();
        for (String movements : allMovements) {
            MoveMessage moveMessage = new MoveMessage();
            moveMessage.setMovements(movements);
            int[] aps = {gameEngine.getPlayers()[0].getActionPoint(), gameEngine.getPlayers()[1].getActionPoint()};
            moveMessage.setCurrentAP(aps);

            MoveMessage[] moveMessages = new MoveMessage[1];
            moveMessages[0] = moveMessage;
            Message message = new Message(Message.NAME_MOVE, moveMessages);
            addMessageToLog(message);
        }
    }

    private List<String> getAllMovements() {
        int num = 0;
        for (Player player : gameEngine.getPlayers()) {
            for (Hero hero : player.getHeroes()) {
                num = Math.max(num, hero.getRecentPath().size());
            }
        }
        StringBuilder[] stringBuilders = new StringBuilder[Math.max(0, num - 1)]; //todo correct?
        for (int i = 0; i < num - 1; i++)
            stringBuilders[i] = new StringBuilder("nnnnnnnn");

        for (Player player : gameEngine.getPlayers()) {
            for (Hero hero : player.getHeroes()) {
                int size = hero.getRecentPath().size();
                if (hero.getHp() <= 0 || size <= 1)
                    continue;
                int id = hero.getId();
                for (int i = size - 1; i < size; i++) {
                    Cell cell = hero.getRecentPath().get(i);
                    Cell prevCell = hero.getRecentPath().get(i - 1);
                    switch (prevCell.getDirectionTo(cell)) {
                        case LEFT:
                            stringBuilders[i - 1].setCharAt(id, 'l');
                            break;
                        case RIGHT:
                            stringBuilders[i - 1].setCharAt(id, 'r');
                            break;
                        case UP:
                            stringBuilders[i - 1].setCharAt(id, 'u');
                            break;
                        case DOWN:
                            stringBuilders[i - 1].setCharAt(id, 'd');
                            break;
                    }
                }
            }
        }

        List<String> ans = new ArrayList<>();
        for (StringBuilder stringBuilder : stringBuilders) {
            ans.add(stringBuilder.toString());
        }
        return ans;
    }

    public void addActionMessage() {
        List<CastedAbility> castedAbilities = gameEngine.getCastedAbilities();
        List<Action> actions = new ArrayList<>();
        for (CastedAbility castedAbility : castedAbilities) {
            Action action = new Action();
            action.setId(castedAbility.getCasterHero().getId());
            action.setRowDistance(castedAbility.getEndCell().getRow() - castedAbility.getStartCell().getRow());
            action.setColumnDistance(castedAbility.getEndCell().getColumn() - castedAbility.getStartCell().getColumn());
            action.setAbility(castedAbility.getAbility().getName());
            actions.add(action);
        }
        ActionMessage actionMessage = new ActionMessage(actions);

        ActionMessage[] actionMessages = new ActionMessage[1];
        actionMessages[0] = actionMessage;
        Message message = new Message(Message.NAME_ACTION, actionMessages);
        addMessageToLog(message);
    }

    public void addStatusMessage() {
        StatusMessage statusMessage = new StatusMessage();
        List<StatusHero> statusHeroes = new ArrayList<>();
        Player[] players = gameEngine.getPlayers();
        for (Player player : players) {
            for (Hero hero : player.getHeroes()) {
                StatusHero statusHero = new StatusHero(hero.getId(), hero.getHp(), hero.getRespawnTime());
                List<RemainingCooldown> remainingCooldowns = new ArrayList<>();
                for (Ability ability : hero.getAbilities()) {
                    remainingCooldowns.add(new RemainingCooldown(ability.getName(), ability.getRemainingCoolDown()));
                }
                statusHero.setRemainingCooldowns(remainingCooldowns);
                statusHeroes.add(statusHero);
            }
        }
        statusMessage.setHeroes(statusHeroes);
        List<RespawnedHero> respawnedHeroes = new ArrayList<>();
        for (Hero hero : gameEngine.getRespawnedHeroes()) {
            respawnedHeroes.add(new RespawnedHero(hero.getId(), hero.getCell().getRow(), hero.getCell().getColumn()));
        }
        statusMessage.setRespawnedHeroes(respawnedHeroes);
        statusMessage.setScores(new int[]{players[0].getScore(), players[1].getScore()});

        StatusMessage[] statusMessages = new StatusMessage[1];
        statusMessages[0] = statusMessage;
        Message message = new Message(Message.NAME_STATUS, statusMessages);
        addMessageToLog(message);
    }

    public void addEndMessage() {
        EndMessage endMessage = new EndMessage();
        int[] scores = new int[CLIENT_NUM];
        for (int i = 0; i < CLIENT_NUM; i++) {
            scores[i] = gameEngine.getPlayers()[i].getScore();
        }
        endMessage.setScores(scores);

        int ap0 = gameEngine.getPlayers()[0].getTotalUsedAp();
        int ap1 = gameEngine.getPlayers()[1].getTotalUsedAp();
        endMessage.setUsedAPs(new int[]{ap0, ap1});

        if (scores[1] > scores[0]) {
            endMessage.setWinner(1);
        }
        else if (scores[0] > scores[1]) {
            endMessage.setWinner(0);
        } else {
            if (ap0 < ap1)
                endMessage.setWinner(0);
            else if (ap1 < ap0)
                endMessage.setWinner(1);
            else
                endMessage.setWinner(-1);
        }

        EndMessage[] endMessages = new EndMessage[]{endMessage};
        Message message = new Message(Message.NAME_END, endMessages);
        addMessageToLog(message);
    }

    private void addMessageToLog(Message message) {
        addMessageToLog(Json.GSON.toJson(message));
    }

    private void addMessageToLog(String logMessage) {
        try {
            logMessage = ",\n" + logMessage + "]";
            logFile.seek(logFile.length() - 1);
            logFile.write(logMessage.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close()
    {
        try
        {
            logFile.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
