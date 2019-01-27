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
    private int maxHp;
    private int hp;
    private Cell cell;
    private List<Cell> recentPath;
    private List<Cell> recentPathForOpponent;
    private int respawnTime;
    public static int MAX_RESPAWN_TIME = 10;
    private int id;         // todo make final
    private String name;    // todo make final

    public void addToRecentPathForOpponent(Cell cell) {
        recentPathForOpponent.add(cell);
    }

    public void moveTo(Cell targetCell) {
        if (cell != null) {
            List<Hero> heroes = cell.getHeroes();
            heroes.remove(this);
        }
        cell = targetCell;
        if (cell != null)
            cell.getHeroes().add(this);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        List<Ability> abilities = new ArrayList<>();
        for (Ability ability : this.abilities) {
            abilities.add((Ability) ability.clone());
        }

        return new Hero(abilities,maxHp, hp, cell, null, null , this.respawnTime, this.id, this.name);
    }
}
