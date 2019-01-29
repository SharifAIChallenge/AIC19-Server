package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.client.ClientCell;
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

	public void init(ClientCell[][] cells)
	{
		numberOfRows = cells.length;
		numberOfColumns = cells[0].length;

		for (ClientCell[] row : cells)
		{
			for (ClientCell clientCell : row)
			{
				Cell cell = new Cell(clientCell);

				if (clientCell.isInMyRespawnZone())
				{
					player1RespawnZone.add(cell);
				}

				if (clientCell.isInOppRespawnZone())
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
}
