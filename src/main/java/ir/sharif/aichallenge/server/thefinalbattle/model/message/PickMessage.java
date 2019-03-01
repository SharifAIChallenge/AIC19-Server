package ir.sharif.aichallenge.server.thefinalbattle.model.message;

import ir.sharif.aichallenge.server.thefinalbattle.model.client.hero.EmptyHero;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@Setter
@Getter

public class PickMessage {
    private List<EmptyHero> myHeroes;
    private List<EmptyHero> oppHeroes;
    private int currentTurn;
}
