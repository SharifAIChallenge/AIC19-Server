package ir.sharif.aichallenge.server.hamid.model;

import lombok.*;

import java.util.List;
import java.util.Set;

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
	private Set<Cell> vision;	// hashcode and equals not matter


	public void addHero(Hero hero) {
		heroes.add(hero);
	}
	public Hero getHero(int heroId) {
		Hero tempHero = new Hero();
		tempHero.setId(heroId);
		for (Hero hero : heroes) {
			if (hero.equals(tempHero)) {
				return hero;
			}
		}
		return null;
	}
}
