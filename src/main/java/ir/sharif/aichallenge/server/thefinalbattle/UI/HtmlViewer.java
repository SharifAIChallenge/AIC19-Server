package ir.sharif.aichallenge.server.thefinalbattle.UI;

import ir.sharif.aichallenge.server.thefinalbattle.model.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.ability.Ability;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.GameState;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.List;

@Setter
@Getter
public class HtmlViewer
{
    private int currentTurn;
    private int currentMovePhase;
    private GameState state;
    private List<CastedAbility> castedAbilities;
    private Player[] players;
    private Map map;
    private File viewerFile;
    private final static String NEW_LINE = "<br>";
    private final static String ROW_START_TAG = "<tr>";
    private final static String ROW_END_TAG = "</tr>";
    private final static String DATA_START_TAG = "<td>";
    private final static String DATA_END_TAG = "</td>";
    private final static String TABLE_START_TAG = "<table border=\"1\">";
    private final static String TABLE_END_TAG = "</table>";
    private StringBuilder viewLog;

    public HtmlViewer()
    {
        viewerFile = new File("view.html");
        viewerFile.delete();
        try
        {
            viewerFile.createNewFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void updateData(int currentTurn, int currentMovePhase, GameState state,
                           List<CastedAbility> castedAbilities, Player[] players, Map map)
    {
        this.currentTurn = currentTurn;
        this.currentMovePhase = currentMovePhase;
        this.state = state;
        this.castedAbilities = castedAbilities;
        this.players = players;
        this.map = map;
        this.viewLog = new StringBuilder();
    }

    public void viewTurn()
    {
        showTurnNums();
        showCastAbilities();
        showPlayersData();
        showMap();
        viewLog.append(NEW_LINE).append(NEW_LINE).append("####################").append(NEW_LINE).append(NEW_LINE);
        try(FileWriter fw = new FileWriter(viewerFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.print(viewLog.toString());
        } catch (IOException ignored) {
        }
    }

    private void showMap()
    {
        if (state == GameState.PICK || state == GameState.INIT)
        {
            return;
        }

        viewLog.append(TABLE_START_TAG);
        Cell[][] cells = map.getCells();
        for (Cell[] row : cells)
        {
            viewLog.append(ROW_START_TAG);
            for (Cell cell : row)
            {
                String dataStartTag = getDataStartTag(cell);
                String data = getDataString(cell);
                viewLog.append(dataStartTag).append(data).append(DATA_END_TAG);
            }
            viewLog.append(ROW_END_TAG);
        }
        viewLog.append(TABLE_END_TAG);
    }

    private String getDataString(Cell cell)
    {
        if (cell.isWall())
        {
            return "W";
        }

        List<Hero> heroes = cell.getHeroes();
        StringBuilder data = new StringBuilder();

        for (Hero hero : heroes)
        {
            int id = hero.getId();
            String color = id % 2 == 0 ? "maroon" : "blue";
            data.append("<font color=\"").append(color).append("\">").append(id).append("</font>");
        }

        return data.toString();
    }

    private String getDataStartTag(Cell cell)
    {
        if (cell.isWall())
            return "<td bgcolor=\"black\">";
        else if (cell.isObjectiveZone())
            return "<td bgcolor=\"green\">";
        else if (map.getPlayer1RespawnZone().contains(cell))
            return "<td bgcolor=\"red\">";
        else if (map.getPlayer2RespawnZone().contains(cell))
            return "<td bgcolor=\"aqua\">";
        return DATA_START_TAG;
    }

    private void showPlayersData()
    {
        for (int i = 0; i < players.length; i++)
        {
            Player player = players[i];
            int score = player.getScore();
            int AP = player.getActionPoint();
            List<Hero> heroes = player.getHeroes();
            viewLog.append("Player ").append(i + 1).append(":").append(NEW_LINE);
            viewLog.append("Score: ").append(score).append(", ").append("AP: ").append(AP).append(NEW_LINE);
            viewLog.append("Heroes: ").append(NEW_LINE);
            for (int j = 0; j < heroes.size(); j++)
            {
                Hero hero = heroes.get(j);
                viewLog.append(hero.getName()).append(" with id ").append(hero.getId());
                if (j != heroes.size() - 1)
                {
                    viewLog.append(", ");
                }
            }
            viewLog.append(NEW_LINE);
        }
    }

    private void showCastAbilities()
    {
        if (state != GameState.ACTION)
        {
            return;
        }

        for (CastedAbility castedAbility : castedAbilities)
        {
            Hero hero = castedAbility.getCasterHero();
            Ability ability = castedAbility.getAbility();
            Cell startCell = castedAbility.getStartCell();
            Cell endCell = castedAbility.getEndCell();
            viewLog.append("Hero with id ").append(hero.getId()).append(" used ").append(ability.getName())
                    .append(" from ").append(startCell).append(" to ").append(endCell).append(NEW_LINE);
        }
    }

    private void showTurnNums()
    {
        viewLog.append("Phase: ").append(state.name()).append(NEW_LINE);
        viewLog.append("Turn: ").append(currentTurn).append(NEW_LINE);
        if (state == GameState.MOVE)
        {
            viewLog.append("Phase Number: ").append(currentMovePhase).append(NEW_LINE);
        }
    }
}
