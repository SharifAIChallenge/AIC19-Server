package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.ability.DodgeAbility;
import ir.sharif.aichallenge.server.hamid.model.ability.PowerAbiliy;
import lombok.*;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Hero {
	private ArrayList<Ability> abilities;
	private ArrayList<DodgeAbility> dogeAbilities;
	private ArrayList<PowerAbiliy> powerAbiliys;
	private int hp;
	private Cell cell;
	private Path recentPath;
}
