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
    private int hp;     // todo default hp --> HeroConstants Class
    private Cell cell;
    private List<Cell> recentPath;
    private List<Cell> recentPathForOpponent;
    private int responeTime;
    public static int MAX_RESPONE_TIME = 10;

    public void addToRecentPathForOpponent(Cell cell) {
        recentPathForOpponent.add(cell);
    }

    public void moveTo(Cell targetCell) {
        List<Hero> heroes = this.cell.getHeroes();
        heroes.remove(this);
        this.cell = targetCell;
        cell.getHeroes().add(this);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        List<Ability> abilities = new ArrayList<>();
        for (Ability ability : this.abilities) {        //todo consider adding ability children
            abilities.add((Ability) ability.clone());
        }

        return new Hero(abilities,maxHp, hp, cell, null, null , this.responeTime);
    }
}
