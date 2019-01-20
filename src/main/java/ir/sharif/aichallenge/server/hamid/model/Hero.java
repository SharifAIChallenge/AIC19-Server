package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.ability.DodgeAbility;
import ir.sharif.aichallenge.server.hamid.model.ability.PowerAbiliy;
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
    private List<DodgeAbility> dogeAbilities;
    private List<PowerAbiliy> powerAbilities;
    private int hp;
    private Cell cell;
    private List<Path> recentPaths;

    @Override
    public Object clone() throws CloneNotSupportedException {
        List<Ability> abilities = new ArrayList<>();
        List<DodgeAbility> dogeAbilities = new ArrayList<>();
        List<PowerAbiliy> powerAbilities = new ArrayList<>();
        List<Path> recentPaths = new ArrayList<>();
        for (Ability ability : this.abilities) {
            abilities.add((Ability) ability.clone());
        }
        for (DodgeAbility ability : this.dogeAbilities) {
            dogeAbilities.add((DodgeAbility) ability.clone());
        }

        for (PowerAbiliy ability : this.powerAbilities) {
            powerAbilities.add((PowerAbiliy) ability.clone());
        }

        for (Path path : this.recentPaths) {
            recentPaths.add((Path) path.clone());
        }
        return new Hero(abilities, dogeAbilities, powerAbilities, hp, cell, recentPaths);
    }
}
