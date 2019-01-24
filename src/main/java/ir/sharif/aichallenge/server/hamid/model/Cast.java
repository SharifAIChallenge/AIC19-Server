package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Cast {
    private Integer heroId;
    private Ability ability;
    private int targetRow;
    private int targetColumn;
}
