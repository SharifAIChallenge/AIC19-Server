package ir.sharif.aichallenge.server.hamid.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Player {
	private int score;
	private List<Hero> heroes;
	private int actionPoint;
	private List<Cell> vision;
}
