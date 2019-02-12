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
    private RandomAccessFile randomAccessViewFile;
    private int counter = 0;
    private final static String NEW_LINE = "<br>";
    private final static String ROW_START_TAG = "<tr>";
    private final static String ROW_END_TAG = "</tr>";
    private final static String DATA_START_TAG = "<td>";
    private final static String DATA_END_TAG = "</td>";
    private final static String TABLE_START_TAG = "<table border='1'>";
    private final static String TABLE_END_TAG = "</table>";
    private final static String PARAGRAPH_START_TAG = "<p>";
    private final static String PARAGRAPH_END_TAG = "</p>";
    private final static String BOLD_START_TAG = "<b>";
    private final static String BOLD_END_TAG = "</b>";
    private final static String NEXT_PAGE_IF_PART_ONE = "if (page==";
    private final static String NEXT_PAGE_IF_PART_TWO = ") document.getElementById(\"content\").innerHTML = \"";
    private final static String START_CODE = "<title>Viewer</title>\n" +
            "\n" +
            "<style>\n" +
            "p{\n" +
            "font-family:\"Verdana\";\n" +
            "}\n" +
            "</style>\n" +
            "\n" +
            "<div align=\"center\">\n" +
            "    <button onclick=\"previous()\"> Previous </button>\n" +
            "    <button onclick=\"next()\"> Next </button>\n" +
            "    </br>\n" +
            "    <button onclick=\"switch_pause()\" id=\"switch_button\"> Play </button>\n" +
            "    </br>\n" +
            "    <button onclick=\"goto()\" id=\"goto\"> Turn: </button><input type=\"text\" id=\"turn\" value=\"0\">\n" +
            "\n" +
            "</div>\n" +
            "\n" +
            "<div id=\"content\" align=\"center\">\n" +
            "THE FINAL BATTLE HAS BEGUN\n" +
            "</div>\n" +
            "\n" +
            "<script>\n" +
            "    page = 0;\n" +
            "    pause = 1;\n" +
            "    function goto(){\n" +
            "    x = document.getElementById(\"turn\").value;\n" +
            "    x -= 4;\n" +
            "    if (x<0)\n" +
            "    show(x+4);\n" +
            "    else\n" +
            "    show(x*7+4);\n" +
            "    }\n" +
            "    function switch_pause(){\n" +
            "        pause = 1 - pause;\n" +
            "        if (pause == 0)\n" +
            "            document.getElementById(\"switch_button\").innerText = \"Pause\";\n" +
            "        else\n" +
            "            document.getElementById(\"switch_button\").innerText = \"Play\";\n" +
            "    }\n" +
            "    function next(){\n" +
            "        page += 1;\n" +
            "        show(page);\n" +
            "    }\n" +
            "    function previous(){\n" +
            "        page -= 1;\n" +
            "        show(page);\n" +
            "    }\n" +
            "    window.setInterval(function(){\n" +
            "        if (pause == 0)\n" +
            "            next();\n" +
            "    }, 1000);\n" +
            "    function show(pg) {\n" +
            "page=pg;\n";
    private final static String END_CODE = "}</script>";
    private StringBuilder viewLog;

    public HtmlViewer()
    {
        viewerFile = new File("view.html");
        viewerFile.delete();
        try
        {
            viewerFile.createNewFile();
            randomAccessViewFile = new RandomAccessFile(viewerFile, "rw");
            randomAccessViewFile.write((START_CODE + END_CODE).getBytes());
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
        if (counter == 0)
        {
            this.viewLog.append(NEXT_PAGE_IF_PART_ONE).append(counter).append(NEXT_PAGE_IF_PART_TWO);
        } else
        {
            this.viewLog.append("else ").append(NEXT_PAGE_IF_PART_ONE).append(counter).append(NEXT_PAGE_IF_PART_TWO);
        }
        counter++;
    }

    public void viewTurn()
    {
        showMap();
        viewLog.append(PARAGRAPH_START_TAG);
        showTurnNums();
        showCastAbilities();
        showPlayersData();
        viewLog.append(PARAGRAPH_END_TAG);
        viewLog.append("\";\n").append(END_CODE);
        writeToFile();
    }

    public void close()
    {
        try
        {
            randomAccessViewFile.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void writeToFile()
    {
        try
        {
            randomAccessViewFile.seek(randomAccessViewFile.length() - 10);
            randomAccessViewFile.write(viewLog.toString().getBytes());
        } catch (IOException e)
        {
            e.printStackTrace();
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
        viewLog.append(TABLE_END_TAG).append(NEW_LINE);
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
            data.append("<font color='").append(color).append("'>").append(id).append("</font>");
        }

        return data.toString();
    }

    private String getDataStartTag(Cell cell)
    {
        if (cell.isWall())
            return "<td bgcolor='black'>";
        else if (cell.isObjectiveZone())
            return "<td bgcolor='green'>";
        else if (map.getPlayer1RespawnZone().contains(cell))
            return "<td bgcolor='red'>";
        else if (map.getPlayer2RespawnZone().contains(cell))
            return "<td bgcolor='aqua'>";
        return DATA_START_TAG;
    }

    private void showPlayersData()
    {
        for (int i = 0; i < players.length; i++)
        {
            Player player = players[i];
            int score = player.getScore();
            int AP = player.getActionPoint();
            int usedAP = player.getTotalUsedAp();
            List<Hero> heroes = player.getHeroes();
            viewLog.append(BOLD_START_TAG).append("Player ").append(i + 1).append(BOLD_END_TAG).append(":")
                    .append(NEW_LINE);
            viewLog.append(BOLD_START_TAG).append("Score").append(BOLD_END_TAG).append(": ")
                    .append(score).append("  -  ").append(BOLD_START_TAG).append("Used AP").append(BOLD_END_TAG)
                    .append(": ").append(usedAP).append(NEW_LINE).append(BOLD_START_TAG).append("AP")
                    .append(BOLD_END_TAG).append(": ").append(AP).append(NEW_LINE);
            viewLog.append(BOLD_START_TAG).append("Heroes").append(BOLD_END_TAG).append(": ").append(NEW_LINE);
            for (int j = 0; j < heroes.size(); j++)
            {
                Hero hero = heroes.get(j);
                String name = hero.getName();
                int id = hero.getId();
                int hp = hero.getHp();
                viewLog.append(id).append("_").append(name).append("_").append(hp).append("hp");
                if (j != heroes.size() - 1)
                {
                    viewLog.append(" . ");
                }
            }
            viewLog.append(NEW_LINE);
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
        viewLog.append(BOLD_START_TAG).append("Phase").append(BOLD_END_TAG).append(": ").append(state.name())
                .append(NEW_LINE);
        viewLog.append(BOLD_START_TAG).append("Turn").append(BOLD_END_TAG).append(": ").append(currentTurn)
                .append(NEW_LINE);
        if (state == GameState.MOVE)
        {
            viewLog.append(BOLD_START_TAG).append("Phase Number").append(BOLD_END_TAG).append(": ")
                    .append(currentMovePhase).append(NEW_LINE);
        }
        viewLog.append(NEW_LINE);
        viewLog.append(NEW_LINE);
    }
}
