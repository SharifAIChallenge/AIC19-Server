package ir.sharif.aichallenge.server.hamid.model;


import ir.sharif.aichallenge.server.hamid.model.enums.Direction;
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
}
