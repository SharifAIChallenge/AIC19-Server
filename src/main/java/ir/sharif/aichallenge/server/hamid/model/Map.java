package ir.sharif.aichallenge.server.hamid.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ir.sharif.aichallenge.server.hamid.model.client.ClientInitialCell;
import lombok.*;

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
	private List<Cell> player1RespawnZone;
	private List<Cell> player2RespawnZone;
	private List<Cell> ObjectiveZone;

	public Cell getCell(int row, int column) {
		return cells[row][column];
	}

	public boolean isInMap(int row, int column)
	{
		return (row >= 0 && row < numberOfRows && column >= 0 && column < numberOfColumns);
	}

	public void init(ClientInitialCell[][] cells)
	{
		numberOfRows = cells.length;
		numberOfColumns = cells[0].length;

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
					ObjectiveZone.add(cell);
				}
			}
		}
	}

    public JsonArray getClientInitialMap(int clientNum)
    {
        String firstClientPropertyName = clientNum == 1 ? "isInMyRespawnZone" : "isInOppRespawnZone";
        String secondClientPropertyName = clientNum == 1 ? "isInOppRespawnZone" : "isInMyRespawnZone";
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

        return cellsArray;
    }
}
