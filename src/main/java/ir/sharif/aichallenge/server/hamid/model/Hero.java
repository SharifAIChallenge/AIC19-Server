package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.ability.DodgeAbility;
import ir.sharif.aichallenge.server.hamid.model.ability.PowerAbiliy;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Hero {
	private List<Ability> abilities;
	private List<DodgeAbility> dogeAbilities;
	private List<PowerAbiliy> powerAbiliys;
	private int hp;
	private Cell cell;
	private List<Path> recentPaths;
}
