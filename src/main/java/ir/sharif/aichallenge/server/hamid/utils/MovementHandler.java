package ir.sharif.aichallenge.server.hamid.utils;

import ir.sharif.aichallenge.server.hamid.model.Cell;
import ir.sharif.aichallenge.server.hamid.model.Hero;
import ir.sharif.aichallenge.server.hamid.model.Map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovementHandler
{
    public static List<HeroMovement> getValidMovements(List<HeroMovement> heroMovements, Map map,
                                                       List<Hero> playerHeroes) // TODO add no movement
    {
        List<HeroMovement> topFour = extractTopFourMovements(heroMovements);
        Set<Cell> heroCurrentCells = extractHeroCurrentCells(playerHeroes);
        if (!isValid(topFour))
        {
            return sequentialMovement(heroMovements, map, heroCurrentCells);
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

    private static List<HeroMovement> sequentialMovement(List<HeroMovement> heroMovements, Map map, Set<Cell> heroCurrentCells)
    {
        Set<Cell> emptyCells = new HashSet<>();
        Set<Cell> fullCells = new HashSet<>();
        Set<Hero> movedHeroes = new HashSet<>();
        List<HeroMovement> finalMovements = new ArrayList<>();

        for (HeroMovement movement : heroMovements)
        {
            Hero hero = movement.getHero();
            Cell targetCell = movement.getEndCell();
            if (fullCells.contains(targetCell) || targetCell.isWall() || movedHeroes.contains(hero) ||
                    (!emptyCells.contains(targetCell) && heroCurrentCells.contains(targetCell)))
            {
                continue;
            }

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

    private static List<HeroMovement> extractTopFourMovements(List<HeroMovement> heroMovements)
    {
        List<HeroMovement> finalMovements = new ArrayList<>();
        Set<Hero> heroes = new HashSet<>();

        for (HeroMovement movement : heroMovements)
        {
            Hero hero = movement.getHero();
            if (heroes.contains(hero))
            {
                continue;
            }
            heroes.add(hero);
            finalMovements.add(movement);
        }

        return finalMovements;
    }


}
