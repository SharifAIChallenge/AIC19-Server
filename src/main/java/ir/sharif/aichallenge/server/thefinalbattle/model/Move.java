package ir.sharif.aichallenge.server.thefinalbattle.model;


import ir.sharif.aichallenge.server.thefinalbattle.model.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Move {
    private List<Direction> moves;
    private Hero hero;

    public void merge(Move move)
    {
        moves.addAll(move.getMoves());
    }

    public int getGreedyApCost() {
        return hero.getMoveApCost() * moves.size();
    }
}
