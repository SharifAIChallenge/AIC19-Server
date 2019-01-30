package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.enums.GameState;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class ClientTurnMessage {
    private GameState type;
    private List<Integer> heroesId;
    private List<Move> moves;
    private String heroName;
    private List<Cast> casts;

    public void mergeMoves()
    {
        Map<Hero, Move> movesMap = new HashMap<>();

        for (Move move : moves)
        {
            Hero hero = move.getHero();
            if (!movesMap.containsKey(hero))
            {
                movesMap.put(hero, new Move(new ArrayList<>(), hero));
            }
            Move heroMainMove = movesMap.get(hero);

        }
    }
}
