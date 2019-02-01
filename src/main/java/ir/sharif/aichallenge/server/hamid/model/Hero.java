package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.ability.Ability;
import ir.sharif.aichallenge.server.hamid.model.client.ClientHeroConstants;
import ir.sharif.aichallenge.server.hamid.model.client.EmptyCell;
import ir.sharif.aichallenge.server.hamid.model.client.hero.ClientHero;
import ir.sharif.aichallenge.server.hamid.model.client.hero.Cooldown;
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
    private int moveApCost;
    private int maxHp;
    private int hp;
    private Cell cell;
    private List<Cell> recentPath;
    private List<Cell> recentPathForOpponent;
    private int maxRespawnTime;
    private int respawnTime;
    private int id;         // todo make final
    private String name;    // todo make final

    public Hero(ClientHeroConstants heroConstant, List<Ability> abilities)
    {
        this.abilities = abilities;
        this.maxHp = heroConstant.getMaxHP();
        this.maxRespawnTime = heroConstant.getRespawnTime();
        this.name = heroConstant.getName();
        this.moveApCost = heroConstant.getMoveAPCost();
        this.hp = this.maxHp;
        this.respawnTime = 0;
    }

    public ClientHero getClientHero()
    {
        ClientHero clientHero = new ClientHero();
        clientHero.setId(this.getId());
        clientHero.setType(this.getName());
        clientHero.setCurrentHP(this.getHp());
        //cooldowns
        List<Cooldown> cooldowns = new ArrayList<>();
        for (Ability ability : this.getAbilities()) {
            Cooldown cooldown = new Cooldown(ability.getName(), ability.getRemainingCoolDown());
            cooldowns.add(cooldown);
        }
        Cooldown[] cool = new Cooldown[cooldowns.size()];
        cool = cooldowns.toArray(cool);
        clientHero.setCooldowns(cool);  //end of cooldowns
        clientHero.setCurrentCell(new EmptyCell(this.getCell().getRow(), this.getCell().getColumn()));
        //recent path
        List<EmptyCell> recentPathList = new ArrayList<>();
        for (Cell cell : this.getRecentPath()) {
            EmptyCell emptyCell = new EmptyCell(cell.getRow(), cell.getColumn());
            recentPathList.add(emptyCell);
        }
        EmptyCell[] recentPath = new EmptyCell[recentPathList.size()];
        recentPath = recentPathList.toArray(recentPath);
        clientHero.setRecentPath(recentPath);
        clientHero.setRespawnTime(this.getMaxRespawnTime());
        return clientHero;
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

    public void resetValues() {     //not necessary to reset respawnTime it's reset when a hero dies.
        hp = maxHp;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        List<Ability> abilities = new ArrayList<>();
        for (Ability ability : this.abilities) {
            abilities.add((Ability) ability.clone());
        }
        ids = ids + 1;
        return new Hero(abilities, moveApCost, maxHp, hp, cell, new ArrayList<>(), new ArrayList<>() , maxRespawnTime,
                respawnTime, ids, name);
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
