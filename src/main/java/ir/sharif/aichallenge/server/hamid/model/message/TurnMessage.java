package ir.sharif.aichallenge.server.hamid.model.message;

import ir.sharif.aichallenge.server.hamid.model.Hero;
import ir.sharif.aichallenge.server.hamid.model.client.ClientMap;
import ir.sharif.aichallenge.server.hamid.model.client.Wall;
import lombok.*;

import java.util.List;

@Data
//@Builder
//@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class TurnMessage {
    private int myScore;
    private int oppScore;
    private String currentPhase;
    private int currentTurn;
    private ClientMap map;
    private List<Hero> myHeroes;
    private List<Hero> oppHeroes;
    private List<Wall> brokenWalls;
    private List<Wall> createdWalls;

}
