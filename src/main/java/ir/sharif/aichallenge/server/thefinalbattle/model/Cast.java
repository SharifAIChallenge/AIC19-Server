package ir.sharif.aichallenge.server.thefinalbattle.model;

import ir.sharif.aichallenge.server.thefinalbattle.model.ability.Ability;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.AbilityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Cast> extractCasts(List<Cast> casts, AbilityType abilityType) {
        List<Cast> extractedCasts = new ArrayList<>();
        for (Cast cast : casts) {
            if (cast.getAbility().getType() == abilityType)
                extractedCasts.add(cast);
        }
        return extractedCasts;
    }
}
