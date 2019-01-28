package ir.sharif.aichallenge.server.hamid.model.message;

import ir.sharif.aichallenge.server.hamid.model.client.CastedAbility;
import ir.sharif.aichallenge.server.hamid.model.client.ClientMap;
import ir.sharif.aichallenge.server.hamid.model.client.hero.ClientHero;
import lombok.*;

import java.util.List;

@Data
//@Builder
//@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class TurnMessage extends Message{
    private int myScore;
    private int oppScore;
    private String currentPhase;
    private int currentTurn;
    private ClientMap map;
    private List<ClientHero> myHeroes;      //todo should be array?
    private List<ClientHero> oppHeroes;     //todo should be array?
    private List<CastedAbility> castAbilities;
}
