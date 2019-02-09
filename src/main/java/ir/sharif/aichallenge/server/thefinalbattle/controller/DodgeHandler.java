package ir.sharif.aichallenge.server.thefinalbattle.controller;

import ir.sharif.aichallenge.server.thefinalbattle.model.*;
import ir.sharif.aichallenge.server.thefinalbattle.model.enums.AbilityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DodgeHandler {
    private Map map;
    private List<Cast> dodgeCasts;
    private Set<Cast> tmpCasts;
    private Player player;
    private java.util.Map<Hero, Cell> mainHeroCells;

    public DodgeHandler(Map map, Player player, List<Cast> casts) {
        this.map = map;
        this.player = player;
        this.dodgeCasts = Cast.extractCasts(casts, AbilityType.DODGE);
    }

    public Set<Cast> getValidDodgeCasts() {
        Set<Cast> okCasts = new HashSet<>();
        fillMainHeroCells();
        int remainingAp = player.getActionPoint();

        for (Cast cast : dodgeCasts) {
            if (okCasts.contains(cast))
                continue;
            tmpCasts = new HashSet<>();
            if (dfs(cast, remainingAp)) {
                for (Cast doneCast : tmpCasts) {
                    remainingAp -= doneCast.getAbility().getApCost();
                }
                okCasts.addAll(tmpCasts);
            }
            else {
                for (Cast wrongCast : tmpCasts) {
                    resetPlace(wrongCast.getHero());
                }
            }
        }

        for (Hero hero : player.getHeroes()) {
            resetPlace(hero);
        }

        return okCasts;
    }

    private boolean dfs(Cast cast, int ap) {
        Hero dodgerHero = cast.getHero();
        Cell endCell = map.getCell(cast.getTargetRow(), cast.getTargetColumn());
        if (endCell.isWall())   //todo is a valid cell in the map? i.e. not out of map
            return false;
        if (cast.getAbility().getApCost() > ap)
            return false;

        for (Hero hero : player.getHeroes()) {
            if (hero.getCell() != endCell) {
                continue;
            }
            for (Cast nextCast : dodgeCasts) {  //todo dodgeCasts ok?
                if (nextCast.getHero() != hero) {
                    continue;
                }
                if (nextCast.getHero().getCell() == map.getCell(nextCast.getTargetRow(), nextCast.getTargetColumn()))
                    return false;
                dodgerHero.setCell(endCell);
                if (dfs(nextCast, ap - cast.getAbility().getApCost())) {    // then all dodges get ok
                    tmpCasts.add(cast);
                    return true;
                }
                else {
                    resetPlace(dodgerHero);
                    return false;       //todo reset hero cell? --> up
                }
            }
            return false;
        }

        dodgerHero.setCell(endCell);
        tmpCasts.add(cast);
        return true;
    }

    private void resetPlace(Hero hero) {
        hero.setCell(mainHeroCells.get(hero));
    }

    private void fillMainHeroCells() {
        mainHeroCells = new HashMap<>();
        for (Hero hero : player.getHeroes()) {
            mainHeroCells.put(hero, hero.getCell());
        }
    }
}
