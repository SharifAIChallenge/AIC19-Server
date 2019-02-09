package ir.sharif.aichallenge.server.thefinalbattle.model.message;

import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientCastedAbility;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientCell;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.hero.ClientHero;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private int movePhaseNum;
    private int currentTurn;
    private int AP;
    private ClientCell[][] map;
    private List<ClientHero> myHeroes;
    private List<ClientHero> oppHeroes;
    private List<ClientCastedAbility> myCastAbilities;
    private List<ClientCastedAbility> oppCastAbilities;
}
