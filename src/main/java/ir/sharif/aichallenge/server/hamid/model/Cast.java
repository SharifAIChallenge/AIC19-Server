package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.enums.AbilityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Cast implements Comparable{
    private Hero hero;
    private Ability ability;
    private int targetRow;
    private int targetColumn;

    @Override
    public int compareTo(Object o) {
        if (o == null)
            return 1;
        if (o instanceof Cast) {
            Cast cast = (Cast) o;
            return this.ability.getType().getPriority() - cast.ability.getType().getPriority();
        } else {
            return 1;
        }
    }
}
