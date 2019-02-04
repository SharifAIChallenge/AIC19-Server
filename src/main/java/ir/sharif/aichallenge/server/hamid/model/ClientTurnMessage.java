package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.enums.Direction;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;
import lombok.*;

import java.util.*;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class ClientTurnMessage {
    private GameState type;
    private List<Integer> heroesId;     //todo what the fuck is it doing here? why we don't have Player instead?
    private List<Move> moves = new ArrayList<>();
    private String heroName;
    private List<Cast> casts = new ArrayList<>();

    public void mergeMoves()
    {
/*        Map<Hero, Move> movesMap = new HashMap<>();
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
        moves.addAll(finalMoves);*/

        Set<Hero> heroes = new HashSet<>();
        List<Move> finalMoves = new ArrayList<>();

        for (Move move : moves) {
            if (heroes.contains(move.getHero()))
                continue;
            heroes.add(move.getHero());
            List<Direction> directions = move.getMoves();
            Direction direction;
            if (directions.size() > 0) {
                direction = directions.get(0);
                directions.clear();
                directions.add(direction);
            }

            finalMoves.add(move);
        }

        moves.clear();
        moves.addAll(finalMoves);
    }
}
