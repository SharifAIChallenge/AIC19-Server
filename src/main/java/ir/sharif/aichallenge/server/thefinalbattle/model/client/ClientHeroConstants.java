package ir.sharif.aichallenge.server.thefinalbattle.model.client;

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

    public ClientHeroConstants(ClientHeroConstants heroConstant)
    {
        switch (heroConstant.getName())
        {
            case "SENTRY":
                this.name = "Ancient Warrior";
                break;
            case "BLASTER":
                this.name = "Red Demon";
                break;
            case "GUARDIAN":
                this.name = "Big Ork";
                break;
            case "HEALER":
                this.name = "Ancient Queen";
                break;
            case "Ancient Warrior":
                this.name = "Mechanical Golem";
                break;
            case "Red Demon":
                this.name = "Slayer";
                break;
            case "Big Ork":
                this.name = "Elemental Golem";
                break;
            case "Ancient Queen":
                this.name = "Mystic";
                break;
        }

        this.abilityNames = heroConstant.getAbilityNames();
        this.moveAPCost = heroConstant.getMoveAPCost();
        this.respawnTime = heroConstant.getRespawnTime();
        this.maxHP = heroConstant.maxHP;
    }
}
