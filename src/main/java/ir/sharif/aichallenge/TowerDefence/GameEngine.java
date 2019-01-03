package ir.sharif.aichallenge.TowerDefence;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ir.sharif.aichallenge.Common.model.Event;
import ir.sharif.aichallenge.Common.network.Json;
import ir.sharif.aichallenge.Common.network.data.Message;
import ir.sharif.aichallenge.Server.server.config.FileParam;
import ir.sharif.aichallenge.Server.server.core.GameLogic;
import ir.sharif.aichallenge.Server.server.core.GameServer;
import ir.sharif.aichallenge.TowerDefence.Factory.Constants;
import ir.sharif.aichallenge.TowerDefence.Map.Map;


import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Future on 1/21/18.
 */
public class GameEngine implements GameLogic
{
    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");
    public static int PARAM_CLIENT_TIMEOUT = 350;
    public static int PARAM_TURN_TIMEOUT = 400;

    private RandomAccessFile logFile;
    private Scenario firstScenario;
    private Scenario secondScenario;
    private ArrayList<Scenario> scenarios = new ArrayList<>();
    private Player p1;
    private Player p2;
    private ArrayList<Player> players = new ArrayList<>();
    private int maxTurns;
    private static AtomicInteger currentTurn = new AtomicInteger(0);
    private Gson gson = new Gson();
    private TurnEvents turnEvents;
    private boolean isHeavyTurn = false;

    public static void main(String[] args) throws InterruptedException
    {
        GameServer gameServer = new GameServer(new GameEngine(), args, currentTurn);
        gameServer.start();
        gameServer.waitForFinish();
    }

    @Override
    public int getClientsNum()
    {
        return 2;
    }

    @Override
    public long getClientResponseTimeout()
    {
        return PARAM_CLIENT_TIMEOUT;
    }

    @Override
    public long getTurnTimeout()
    {
        return PARAM_TURN_TIMEOUT;
    }

    @Override
    public void init()
    {
        String initStr = readMapFile(PARAM_MAP);
        JsonObject initJson = null;
        try
        {
            initJson = gson.fromJson(initStr, JsonArray.class).get(0).getAsJsonObject();
        } catch (JsonSyntaxException e)
        {
            System.err.println("Invalid map file!");
            System.exit(0);
        }

        Constants.setConsts(initJson.getAsJsonArray("params"));
        Map firstMap = createMap(initJson);
        Map secondMap = createMap(initJson);
        createScenarios(firstMap, secondMap);
    }

    private String readMapFile(FileParam paramMap)
    {
        String result = "";
        File mapFile = paramMap.getValue();
        if (mapFile == null || !mapFile.exists())
        {
            System.err.println("Invalid map file!");
            System.exit(0);
        }
        try (Scanner in = new Scanner(mapFile))
        {
            while (in.hasNext())
            {
                result += in.nextLine();
                result += "\n";
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private void createScenarios(Map firstMap, Map secondMap)
    {
        turnEvents = new TurnEvents();
        p1 = new Player(0, turnEvents);
        p2 = new Player(1, turnEvents);
        players.add(p1);
        players.add(p2);
        p1.setOpponent(p2);
        p2.setOpponent(p1);

        firstScenario = new Scenario(p1, p2, firstMap, turnEvents);
        secondScenario = new Scenario(p2, p1, secondMap, turnEvents);
        scenarios.add(firstScenario);
        scenarios.add(secondScenario);
        maxTurns = Constants.NUMBER_OF_TURNS;
    }

    private Map createMap(JsonObject initJson)
    {
        JsonObject mapCellsData = initJson.getAsJsonObject("map");
        JsonArray pathsData = initJson.getAsJsonArray("paths");

        return new Map(mapCellsData, pathsData);
    }

    @Override
    public Message getUIInitialMessage()
    {
        return getInitMessage();
    }

    @Override
    public Message[] getClientInitialMessages()
    {
        Message[] messages = new Message[2];
        messages[0] = getInitMessage();
        messages[1] = getInitMessage();

        try
        {
            File prevLogFile = new File("Game.txt");
            if (prevLogFile.exists())
            {
                prevLogFile.delete();
            }
            logFile = new RandomAccessFile("Game.txt", "rw");
            String logStr = "[" + Json.GSON.toJson(messages[0]) + "]";
            logFile.write(logStr.getBytes());

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return messages;
    }

    private Message getInitMessage()
    {
        JsonArray resultArray = new JsonArray();
        JsonObject resultObject = new JsonObject();

        JsonObject mapJson = firstScenario.getMap().getMapCellsData();
        JsonArray pathsJson = firstScenario.getMap().getPathsData();
        JsonArray constsJson = Constants.getConstantsJson();

        resultObject.add("map", mapJson);
        resultObject.add("paths", pathsJson);
        resultObject.add("params", constsJson);

        resultArray.add(resultObject);

        return new Message(Message.NAME_INIT, resultArray);
    }


    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent)
    {
//        if (!checkEquality(clientsEvent[0], clientsEvent[1]))
//        {
//            System.out.println("ERROR IN INPUT FROM CLIENTS");
//        }

        moveToNextTurn();
//        System.out.println("Got message in turn " + currentTurn);

        // TODO clean this place :| well i guess its too late
        ArrayList<ArrayList<Pair<Character, Integer>>> allUnitCreationData = new ArrayList<>();
        ArrayList<ArrayList<Pair<Character, int[]>>> allTowerCreation = new ArrayList<>();
        ArrayList<ArrayList<Integer>> allTowerUpgradeData = new ArrayList<>();
        ArrayList<ArrayList<int[]>> allNukeData = new ArrayList<>();
        ArrayList<ArrayList<int[]>> allBeanData = new ArrayList<>();

        for (int i = 0; i < clientsEvent.length; i++)
        {
            ArrayList<Pair<Character, Integer>> unitCreationData = new ArrayList<>();
            ArrayList<Pair<Character, int[]>> towerCreationData = new ArrayList<>();
            ArrayList<Integer> towerUpgradeDate = new ArrayList<>();
            ArrayList<int[]> nukeData = new ArrayList<>();
            ArrayList<int[]> beanData = new ArrayList<>();

            for (Event event : clientsEvent[i])
            {
//                System.out.println("Message in turn " + currentTurn + " is not empty");
                try
                {
//                    System.out.println("event Type:" + event.getType());
                    switch (event.getType())
                    {
                        case "cu":
//                            System.out.println("Create Unit Received!");
                            unitCreationData.add(parseUnitCreationData(event.getArgs()));
                            break;
                        case "ct":
//                            System.out.println("Create Tower Received!");
                            towerCreationData.add(parseTowerCreationData(event.getArgs()));
                            break;
                        case "ut":
//                            System.out.println("Upgrade Tower Received!");
                            towerUpgradeDate.add(parseTowerUpgradeData(event.getArgs()));
                            break;
                        case "s":
//                            System.out.println("Storm Received!");
                            nukeData.add(parseSpecialEventData(event.getArgs()));
                            break;
                        case "b":
//                            System.out.println("Bean Received!");
                            beanData.add(parseSpecialEventData(event.getArgs()));
                            break;
                        case "end":         // Useless
                            break;
                        default:
                            throw new InvalidEventException("Event Type Error: event.getType() is invalid");
                    }
                } catch (InvalidEventException ignored)
                {

                }
            }
            allUnitCreationData.add(unitCreationData);
            allTowerCreation.add(towerCreationData);
            allTowerUpgradeData.add(towerUpgradeDate);
            allNukeData.add(nukeData);
            allBeanData.add(beanData);
        }

        tick(allUnitCreationData, allTowerCreation, allTowerUpgradeData, allNukeData, allBeanData);
    }

    private boolean checkEquality(Event[] firstEvents, Event[] secondEvents)
    {
        if (firstEvents.length != secondEvents.length)
        {
            return false;
        }

        for (int i = 0; i < firstEvents.length; i++)
        {
            if (!firstEvents[i].getType().equals(secondEvents[i].getType()))
            {
                return false;
            }
            if (firstEvents[i].getType().equals("ut") || firstEvents[i].getType().equals("b"))
            {
                continue;
            }
            if (!Json.GSON.toJson(firstEvents[i].getArgs()).equals(Json.GSON.toJson(secondEvents[i].getArgs())))
            {
                return false;
            }
        }

        return true;
    }

    private void moveToNextTurn()
    {
        currentTurn.incrementAndGet();

        if (currentTurn.get() % 10 == 9)
        {
            PARAM_CLIENT_TIMEOUT = 1200;
            PARAM_TURN_TIMEOUT = 2000;
            isHeavyTurn = true;
            return;
        }
        if (PARAM_TURN_TIMEOUT == 2000)
        {
            isHeavyTurn = false;
            PARAM_CLIENT_TIMEOUT = 350;
            PARAM_TURN_TIMEOUT = 400;
        }
    }

    private void tick(ArrayList<ArrayList<Pair<Character, Integer>>> allUnitCreationData,
                      ArrayList<ArrayList<Pair<Character, int[]>>> allTowerCreation,
                      ArrayList<ArrayList<Integer>> allTowerUpgradeData,
                      ArrayList<ArrayList<int[]>> allNukeData, ArrayList<ArrayList<int[]>> allBeanData)
    {
        firstScenario.createTowers(allTowerCreation.get(0));
        secondScenario.createTowers(allTowerCreation.get(1));

        firstScenario.upgradeTowers(allTowerUpgradeData.get(0));
        secondScenario.upgradeTowers(allTowerUpgradeData.get(1));

        firstScenario.updateUnitsVision();
        secondScenario.updateUnitsVision();

        firstScenario.nuke(allNukeData.get(0));
        secondScenario.nuke(allNukeData.get(1));

        firstScenario.plantBean(allBeanData.get(1));
        secondScenario.plantBean(allBeanData.get(0));

        firstScenario.triggerTowers();
        secondScenario.triggerTowers();

        firstScenario.moveUnits();
        secondScenario.moveUnits();

        firstScenario.createUnits(allUnitCreationData.get(1));
        secondScenario.createUnits(allUnitCreationData.get(0));

        firstScenario.updateUnitsVision();
        secondScenario.updateUnitsVision();

        if (isHeavyTurn)
        {
            p1.addIncome();
            p2.addIncome();
        }
    }

    private int[] parseSpecialEventData(String[] data) throws InvalidEventException
    {
        if (data.length != 2)
            throw new InvalidEventException("Special Event Error: data.length != 2");

        int[] location = new int[2];

        try
        {
            location[0] = Integer.parseInt(data[0]);
            location[1] = Integer.parseInt(data[1]);
        } catch (NumberFormatException e)
        {
            throw new InvalidEventException();
        }

        return location;
    }

    private Integer parseTowerUpgradeData(String[] data) throws InvalidEventException
    {
        if (data.length != 1)
            throw new InvalidEventException("Upgrade Tower Error: data.length != 1");

        int towerId;
        try
        {
            towerId = Integer.parseInt(data[0]);
        } catch (NumberFormatException e)
        {
            throw new InvalidEventException();
        }

        return towerId;
    }

    private Pair<Character, int[]> parseTowerCreationData(String[] data) throws InvalidEventException
    {
        if (data.length != 4 || data[0].length() != 1 || (data[0].charAt(0) != 'a' && data[0].charAt(0) != 'c'))
            throw new InvalidEventException("Create Tower Error: Invalid Message");

        int towerLv;
        int x;
        int y;
        Character towerType = data[0].charAt(0);

        try
        {
            towerLv = Integer.parseInt(data[1]);
            x = Integer.parseInt(data[2]);
            y = Integer.parseInt(data[3]);
        } catch (NumberFormatException e)
        {
            throw new InvalidEventException("Create Tower Input Data Type is invalid");
        }
        return new Pair<>(towerType, new int[]{x, y, towerLv});
    }

    private Pair<Character, Integer> parseUnitCreationData(String[] data) throws InvalidEventException
    {
        if (data.length != 2 || data[0].length() != 1)
            throw new InvalidEventException("Create Unit Error: data.length != 1");

        int unitCount;
        Character unitType = data[0].charAt(0);
        if (unitType != 'l' && unitType != 'h')
            throw new InvalidEventException("Create Unit Error: Invalid unitType");

        try
        {
            unitCount = Integer.parseInt(data[1]);
        } catch (NumberFormatException e)
        {
            throw new InvalidEventException();
        }

        return new Pair<>(unitType, unitCount);
    }

    @Override
    public void generateOutputs()
    {
        // Lol wut?!
    }

    @Override
    public Message getUIMessage()
    {
        JsonArray jsonElements = new JsonArray();
        JsonObject resultObject = new JsonObject();
        resultObject.add("units", firstScenario.getUnitJsonForUI());
        resultObject.add("towers", firstScenario.getTowerJsonForUI());
        resultObject.add("towers_partial", firstScenario.getTowerPartialJsonForUI());
        resultObject.add("players", firstScenario.getPlayerJsonForUI());
        resultObject.add("events", firstScenario.getEventJsonForUI());
        jsonElements.add(resultObject);

        Message message = new Message(Message.NAME_TURN, jsonElements);
        try
        {
            String logStr = ",\n" + Json.GSON.toJson(message) + "]";
            addMessageToLog(logStr);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return new Message(Message.NAME_TURN, jsonElements);
    }

    @Override
    public Message getStatusMessage()
    {
        Message message = new Message(Message.NAME_STATUS, new Object[]{currentTurn.get(), p1.getHealth(), p2.getHealth()});
        try
        {
            String logStr = ",\n" + Json.GSON.toJson(message) + "]";
            addMessageToLog(logStr);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return message;
    }

    @Override
    public Message[] getClientMessages()
    {
        Message[] messages = new Message[2];
        for (int i = 0; i < 2; i++)
        {
            messages[i] = new Message(Message.NAME_TURN, createMsgJson(i));
        }

        for (Player player : players)
        {
            player.clearTemporaryData();
        }

        return messages;
    }

    private JsonArray createMsgJson(int playerId)
    {
        JsonArray resultArray = new JsonArray();
        Player player = players.get(playerId);
        JsonObject resultObject = new JsonObject();
        resultObject.add("myunits", player.getJsonUnits());
        resultObject.add("enemyunits", player.getOpponentJsonUnits());
        resultObject.add("mytowers", player.getJsonTowers(player.getTowers(), true));
        resultObject.add("enemytowers", player.getOpponentJsonTowers());
        resultObject.add("players", player.getSelfAndOppData());
        resultObject.add("events", player.getEventsJsonDataForClient());
        resultArray.add(resultObject);

        return resultArray;
    }

    @Override
    public Event[] makeEnvironmentEvents()
    {
        return new Event[0];
    }

    @Override
    public boolean isGameFinished()
    {
        if (currentTurn.get() <= maxTurns - 1)
        {
            if (p1.getHealth() <= 0 && p2.getHealth() > 0)
            {
                p1.setHealth(0);
                finishLog();
                return true;
            } else if (p2.getHealth() <= 0 && p1.getHealth() > 0)
            {
                p2.setHealth(0);
                finishLog();
                return true;
            } else if (p1.getHealth() == p2.getHealth() && p1.getHealth() == 0)
            {
                int p1TotalTransaction = p1.getTurnover();
                int p2TotalTransaction = p2.getTurnover();
                if (p1TotalTransaction > p2TotalTransaction)
                {
                    p1.setHealth(1);
                    p2.setHealth(0);
                    finishLog();
                    return true;
                } else if (p1TotalTransaction < p2TotalTransaction)
                {
                    p1.setHealth(0);
                    p2.setHealth(1);
                    finishLog();
                    return true;
                } else
                {
                    p1.setHealth(0);
                    p2.setHealth(0);
                    finishLog();
                    return true;
                }
            } else
            {
                return false;
            }
        } else if (currentTurn.get() >= maxTurns - 1)
        {
            if (p1.getHealth() <= 0 && p2.getHealth() > 0)
            {
                p1.setHealth(0);
                finishLog();
            } else if (p2.getHealth() <= 0 && p1.getHealth() > 0)
            {
                p2.setHealth(0);
                finishLog();
            } else
            {
                if (p1.getHealth() < p2.getHealth())
                {
                    p1.setHealth(0);
                    finishLog();
                } else if (p1.getHealth() > p2.getHealth())
                {
                    p2.setHealth(0);
                    finishLog();
                } else if (p1.getHealth() == p2.getHealth())
                {
                    int p1TotalTransaction = p1.getTurnover();
                    int p2TotalTransaction = p2.getTurnover();
                    if (p1TotalTransaction > p2TotalTransaction)
                    {
                        p2.setHealth(0);
                        finishLog();
                    } else if (p1TotalTransaction < p2TotalTransaction)
                    {
                        p1.setHealth(0);
                        finishLog();
                    } else
                    {
                        p1.setHealth(0);
                        p2.setHealth(0);
                        finishLog();
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void finishLog()
    {
        try
        {
            Message message = new Message(Message.NAME_STATUS, new Object[]{currentTurn.get(), p1.getHealth(), p2.getHealth()});
            String logStr = ",\n" + Json.GSON.toJson(message) + "]";
            addMessageToLog(logStr);
            logFile.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void terminate()
    {

    }

    private void addMessageToLog(String logMessage) throws IOException
    {
        logFile.seek(logFile.length() - 1);
        logFile.write(logMessage.getBytes());
    }
}

/*
    By:
    Agha Mohsen
    The Pkms
*/