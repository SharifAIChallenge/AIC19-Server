package ir.sharif.aichallenge.server.hamid.model.message;

import ir.sharif.aichallenge.server.hamid.model.Hero;
import ir.sharif.aichallenge.server.hamid.model.client.hero.EmptyHero;
import lombok.*;

import java.util.List;

@Data
//@Builder
//@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class PickMessage {
    private List<EmptyHero> myHeroes;
    private List<EmptyHero> oppHeroes;
    private int currentTurn;
}
