package ir.sharif.aichallenge.server.hamid.model;

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
}
