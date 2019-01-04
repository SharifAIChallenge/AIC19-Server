package ir.sharif.aichallenge.server.hamid.model;

import lombok.*;

import java.util.ArrayList;

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
	private ArrayList<Cell> player1RespownZone;
	private ArrayList<Cell> player2RespownZone;
	private ArrayList<Cell> ObjectiveZone;
}
