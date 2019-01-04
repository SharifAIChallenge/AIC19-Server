package ir.sharif.aichallenge.server.hamid.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Cell {
	private boolean isWall;
	private boolean isObjectiveZone;
	private Hero[] heroes;
	private int row;
	private int column;
}
