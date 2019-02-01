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
    private List<Move> moves = new ArrayList<>();
    private String heroName;
    private List<Cast> casts = new ArrayList<>();

    public void mergeMoves()
    {
        Map<Hero, Move> movesMap = new HashMap<>();
        List<Move> finalMoves = new ArrayList<>();

        for (Move move : moves)
        {
            Hero hero = move.getHero();
            if (hero.getHp() == 0)
            {
                continue;
            }
            Move heroMainMove = movesMap.get(hero);
            if (!movesMap.containsKey(hero))
            {
                heroMainMove = new Move(new ArrayList<>(), hero);
                movesMap.put(hero, heroMainMove);
                finalMoves.add(heroMainMove);
            }
            heroMainMove.merge(move);
        }

        moves.clear();
        moves.addAll(finalMoves);
    }
}
