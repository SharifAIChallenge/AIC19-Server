package ir.sharif.aichallenge.server.hamid.model.ability;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class PowerAbiliy extends Ability{
	private int areaOfEffect;
	private int power;
	private boolean isLobbing;
}
