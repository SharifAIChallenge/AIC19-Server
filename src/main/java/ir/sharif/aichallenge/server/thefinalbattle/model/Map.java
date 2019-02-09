package ir.sharif.aichallenge.server.thefinalbattle.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientInitialCell;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Map {
	private Cell[][] cells;
	private int numberOfRows;
	private int numberOfColumns;
	private List<Cell> objectiveZone;
	// TODO fields below should go to the Player class
	private List<Cell> player1RespawnZone;
	private List<Cell> player2RespawnZone;

	public Cell getCell(int row, int column) {
		if (!isInMap(row, column))
			return null;
		return cells[row][column];
	}

	public boolean isInMap(int row, int column)
	{
		return (row >= 0 && row < numberOfRows && column >= 0 && column < numberOfColumns);
	}

	public void init(ClientInitialCell[][] cells, Player[] players)
	{
		numberOfRows = cells.length;
		numberOfColumns = cells[0].length;

		this.cells = new Cell[numberOfRows][numberOfColumns];
		this.objectiveZone = new ArrayList<>();
		this.player1RespawnZone = new ArrayList<>();
		this.player2RespawnZone = new ArrayList<>();

		for (ClientInitialCell[] row : cells)
		{
			for (ClientInitialCell clientCell : row)
			{
				Cell cell = new Cell(clientCell);

				if (clientCell.isInFirstRespawnZone())
				{
					player1RespawnZone.add(cell);
				}

				if (clientCell.isInSecondRespawnZone())
				{
					player2RespawnZone.add(cell);
				}

				if (clientCell.isInObjectiveZone())
				{
					objectiveZone.add(cell);
				}
				this.cells[cell.getRow()][cell.getColumn()] = cell;
			}
		}

		players[0].getRespawnZone().addAll(this.player1RespawnZone);
		players[1].getRespawnZone().addAll(this.player2RespawnZone);
	}

    public JsonObject getClientInitialMap(int clientNum)
    {
        String firstClientPropertyName = clientNum == 0 ? "isInMyRespawnZone" : "isInOppRespawnZone";
        String secondClientPropertyName = clientNum == 0 ? "isInOppRespawnZone" : "isInMyRespawnZone";
        JsonObject mapObject = new JsonObject();
        mapObject.addProperty("rowNum", numberOfRows);
        mapObject.addProperty("columnNum", numberOfColumns);
        JsonArray cellsArray = new JsonArray();
        for (Cell[] row : cells)
        {
            JsonArray rowArray = new JsonArray();

            for (Cell cell : row)
            {
                JsonObject cellObject = new JsonObject();
                cellObject.addProperty("isWall", cell.isWall());
                cellObject.addProperty(firstClientPropertyName, player1RespawnZone.contains(cell));
                cellObject.addProperty(secondClientPropertyName, player2RespawnZone.contains(cell));
                cellObject.addProperty("isInObjectiveZone", cell.isObjectiveZone());
                cellObject.addProperty("row", cell.getRow());
                cellObject.addProperty("column", cell.getColumn());
                rowArray.add(cellObject);
            }

            cellsArray.add(rowArray);
        }

        mapObject.add("cells", cellsArray);
        return mapObject;
    }
}
