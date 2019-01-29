package ir.sharif.aichallenge.server.hamid.model.ability;

import ir.sharif.aichallenge.server.hamid.model.enums.AbilityType;
import lombok.*;

import java.util.Objects;

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
	private int coolDown;
	private int remainingCoolDown;

	// power ability
	private int areaOfEffect;
	private int power;
	private boolean isLobbing;
	private boolean isPiercing;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Ability ability = (Ability) o;
		return Objects.equals(name, ability.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
