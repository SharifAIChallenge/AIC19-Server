package ir.sharif.aichallenge.server.hamid.model.ability;

import ir.sharif.aichallenge.server.hamid.model.enums.AbilityType;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Ability {
	private String name;
	private AbilityType type;
	private int range;
	private int apCost;
	private int cooldown;
	private int remainingCooldown;
}
