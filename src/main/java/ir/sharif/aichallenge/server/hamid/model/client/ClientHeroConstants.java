package ir.sharif.aichallenge.server.hamid.model.client;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class ClientHeroConstants
{
    private String name;
    private String[] abilityNames;
    private int maxHP;
    private int moveAPCost;
    private int respawnTime;
}
