package ir.sharif.aichallenge.server.hamid.model;

import lombok.*;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Player {
	private int score;
	private ArrayList<Hero> heroes;
	private int actionPoint;
	private ArrayList<Cell> vision;
}
