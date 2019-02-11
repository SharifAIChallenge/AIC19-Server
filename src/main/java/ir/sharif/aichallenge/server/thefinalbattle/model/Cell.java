package ir.sharif.aichallenge.server.thefinalbattle.model;

import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientInitialCell;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.EmptyCell;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.Direction;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Cell {
	private boolean isWall;
	private boolean isObjectiveZone;
	private List<Hero> heroes = new ArrayList<>();
	private int row;
	private int column;

	public Cell(ClientInitialCell clientCell)
	{
		this.isWall = clientCell.isWall();
		this.isObjectiveZone = clientCell.isInObjectiveZone();
		this.row = clientCell.getRow();
		this.column = clientCell.getColumn();
	}

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Cell cell = (Cell) o;
		return row == cell.row &&
				column == cell.column;
	}

	@Override
	public int hashCode() {
		return Objects.hash(row, column);
	}

	public EmptyCell getEmptyCell()
	{
		return new EmptyCell(this.row, this.column);
	}

	public Direction getDirectionTo(Cell nextCell) {
		int nextRow = nextCell.getRow();
		int nextColumn = nextCell.getColumn();

		if (nextRow == row + 1)
			return Direction.DOWN;
		if (nextRow == row - 1)
			return Direction.UP;
		if (nextColumn == column + 1)
			return Direction.RIGHT;
		if (nextColumn == column - 1)
			return Direction.LEFT;

		return null;
	}

	@Override
	public String toString()
	{
		return "(Row: " + row + ", Column: " + column + ")";
	}
}
