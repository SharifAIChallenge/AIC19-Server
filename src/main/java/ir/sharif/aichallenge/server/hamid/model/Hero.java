package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Hero {
    private List<Ability> abilities;
    private int hp;     // todo default hp --> HeroConstants Class
    private Cell cell;
    private List<Cell> recentPath;
    private List<Cell> recentPathForOpponent;

    @Override
    public Object clone() throws CloneNotSupportedException {
        List<Ability> abilities = new ArrayList<>();
        for (Ability ability : this.abilities) {        //todo consider adding ability children
            abilities.add((Ability) ability.clone());
        }

        return new Hero(abilities, hp, cell, null, null);
    }
}
