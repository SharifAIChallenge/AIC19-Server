package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.client.ClientHeroConstants;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Hero implements Cloneable{
    private static int ids = -1;
    private List<Ability> abilities;
    private int maxHp;
    private int hp;
    private Cell cell;
    private List<Cell> recentPath;
    private List<Cell> recentPathForOpponent;
    private int respawnTime;
    public static int MAX_RESPAWN_TIME = 10; // TODO why is this final?
    private int id;         // todo make final
    private String name;    // todo make final

    public Hero(ClientHeroConstants heroConstant, List<Ability> abilities)
    {
        this.abilities = abilities;
        this.maxHp = heroConstant.getMaxHP();
        this.respawnTime = heroConstant.getRespawnTime();
        this.name = heroConstant.getName();
        this.hp = this.maxHp;
    }

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
        ids = ids + 1;
        return new Hero(abilities,maxHp, hp, cell, null, null , this.respawnTime, ids, this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hero hero = (Hero) o;
        return id == hero.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public Ability getAbility(String name) {
        Ability tempAbility = new Ability();
        tempAbility.setName(name);
        for (Ability ability : abilities) {
            if (ability.equals(tempAbility))
                return tempAbility;
        }
        return null;
    }
}
