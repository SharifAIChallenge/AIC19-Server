package ir.sharif.aichallenge.server.thefinalbattle.model.ability;

import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientAbilityConstants;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.AbilityType;
import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Ability implements Cloneable{
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
	private boolean isPiercing;		//NOTE: this field is for final level in AI Challenge

	public Ability(ClientAbilityConstants abilityConstant)
	{
		this.name = abilityConstant.getName();
		this.type = abilityConstant.getType();
		this.range = abilityConstant.getRange();
		this.apCost = abilityConstant.getAPCost();
		this.coolDown = abilityConstant.getCooldown();
		this.areaOfEffect = abilityConstant.getAreaOfEffect();
		this.power = abilityConstant.getPower();
		this.isLobbing = abilityConstant.isLobbing();
		this.isPiercing = abilityConstant.isPiercing();
		this.remainingCoolDown = 0;
	}

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
