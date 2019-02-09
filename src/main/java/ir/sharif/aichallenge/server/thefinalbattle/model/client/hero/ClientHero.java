package ir.sharif.aichallenge.server.thefinalbattle.model.client.hero;

import ir.sharif.aichallenge.server.thefinalbattle.model.client.EmptyCell;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientHero {
    private int id;
    private String type;    //name
    private int currentHP;
    private Cooldown[] cooldowns;
    private EmptyCell currentCell;
    private EmptyCell[] recentPath;
    private int respawnTime;
}
