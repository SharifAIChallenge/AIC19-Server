package ir.sharif.aichallenge.server.thefinalbattle.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.EmptyCell;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.hero.ClientHero;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
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
	private List<Hero> heroes = new ArrayList<>();
	private int actionPoint;	//todo reset each turn
	private Set<Cell> vision = new HashSet<>();	// hashcode and equals not matter
	private Player opponent;
	private int totalUsedAp = 0;
	private Set<Cell> respawnZone = new HashSet<>();

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

	public void updateServerViewLog(JsonArray playersJson)
	{
		JsonObject playerJson = new JsonObject();
		playerJson.addProperty("score", score);
		List<ClientHero> clientHeroes = new ArrayList<>();
		for (Hero hero : heroes) {
			clientHeroes.add(hero.getClientHero());
		}
		playerJson.add("heroes", Json.GSON.toJsonTree(clientHeroes).getAsJsonArray());
		playerJson.addProperty("ap", actionPoint);
		List<EmptyCell> vision = new ArrayList<>();
		for (Cell cell : this.vision) {
			vision.add(cell.getEmptyCell());
		}
		playerJson.add("vision", Json.GSON.toJsonTree(vision).getAsJsonArray());

		playersJson.add(playerJson);
	}
}
