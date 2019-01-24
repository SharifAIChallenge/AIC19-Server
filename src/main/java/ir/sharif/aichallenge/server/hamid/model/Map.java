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
	private List<Cell> player1RespownZone;
	private List<Cell> player2RespownZone;
	private List<Cell> ObjectiveZone;

	public Cell getCell(int row, int column) {
		return cells[row][column];
	}
}
