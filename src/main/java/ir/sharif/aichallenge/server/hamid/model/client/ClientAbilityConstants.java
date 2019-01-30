package ir.sharif.aichallenge.server.hamid.model.client;

import ir.sharif.aichallenge.server.hamid.model.enums.AbilityType;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class ClientAbilityConstants
{
    private String name;
    private AbilityType type;
    private int range;
    private int APCost;
    private int cooldown;
    private int areaOfEffect;
    private int power;
    private boolean isLobbing;
    private boolean isPiercing;
}
