package ir.sharif.aichallenge.server.thefinalbattle.utils;

import ir.sharif.aichallenge.server.thefinalbattle.model.Cell;
import ir.sharif.aichallenge.server.thefinalbattle.model.Hero;
import ir.sharif.aichallenge.server.thefinalbattle.model.Map;
import ir.sharif.aichallenge.server.thefinalbattle.model.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovementHandler
{
    public static List<HeroMovement> getValidMovements(List<HeroMovement> heroMovements, Map map, Player player)
    {
        List<Hero> playerHeroes = player.getHeroes();
        List<HeroMovement> topFour = extractTopFourMovements(heroMovements, player);
        Set<Cell> heroCurrentCells = extractHeroCurrentCells(playerHeroes);
        if (!isValid(topFour))
        {
            return sequentialMovement(heroMovements, player, heroCurrentCells);
        }
        return topFour;
    }

    private static Set<Cell> extractHeroCurrentCells(List<Hero> playerHeroes)
    {
        Set<Cell> currentCells = new HashSet<>();

        for (Hero hero : playerHeroes)
        {
            currentCells.add(hero.getCell());
        }

        return currentCells;
    }

    private static List<HeroMovement> sequentialMovement(List<HeroMovement> heroMovements, Player player,
                                                         Set<Cell> heroCurrentCells)
    {
        Set<Cell> emptyCells = new HashSet<>();
        Set<Cell> fullCells = new HashSet<>();
        Set<Hero> movedHeroes = new HashSet<>();
        List<HeroMovement> finalMovements = new ArrayList<>();
        int ap = player.getActionPoint();

        for (HeroMovement movement : heroMovements)
        {
            Hero hero = movement.getHero();
            Cell targetCell = movement.getEndCell();
            if (fullCells.contains(targetCell) || targetCell.isWall() || movedHeroes.contains(hero) ||
                    (!emptyCells.contains(targetCell) && heroCurrentCells.contains(targetCell)) || ap < movement.getAp())
            {
                continue;
            }

            ap -= movement.getAp();
            emptyCells.add(hero.getCell());
            fullCells.add(targetCell);
            finalMovements.add(movement);
            movedHeroes.add(hero);
        }

        return finalMovements;
    }

    private static boolean isValid(List<HeroMovement> topFour)
    {
        Set<Cell> targets = new HashSet<>();

        for (HeroMovement movement : topFour)
        {
            Cell cell = movement.getEndCell();
            if (targets.contains(cell) || cell.isWall())
            {
                return false;
            }
            targets.add(movement.getEndCell());
        }

        return true;
    }

    private static List<HeroMovement> extractTopFourMovements(List<HeroMovement> heroMovements, Player player)
    {
        List<HeroMovement> finalMovements = new ArrayList<>();
        Set<Hero> heroes = new HashSet<>();
        int ap = player.getActionPoint();

        for (HeroMovement movement : heroMovements)
        {
            Hero hero = movement.getHero();
            if (heroes.contains(hero))
            {
                continue;
            }

            if (ap - movement.getAp() < 0)
                continue;
            ap -= movement.getAp();
            heroes.add(hero);
            finalMovements.add(movement);
        }

        return finalMovements;
    }


}
