package ir.sharif.aichallenge.server.hamid.model.message;

import ir.sharif.aichallenge.server.hamid.model.client.ClientCastedAbility;
import ir.sharif.aichallenge.server.hamid.model.client.ClientCell;
import ir.sharif.aichallenge.server.hamid.model.client.hero.ClientHero;
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
    private int currentTurn;
    private int AP;
    private ClientCell[][] map;
    private List<ClientHero> myHeroes;
    private List<ClientHero> oppHeroes;
    private List<ClientCastedAbility> myCastAbilities;
    private List<ClientCastedAbility> oppCastAbilities;
}
