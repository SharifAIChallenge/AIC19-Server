package ir.sharif.aichallenge.server.hamid.model;

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
}
