package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.hamid.model.Hero;
import ir.sharif.aichallenge.server.hamid.model.Player;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;

import java.util.ArrayList;
import java.util.Map;

public class GameEngine {

    private Player firstPlayer;
    private Player secondPlayer;
    private GameState state;
    private Map<Integer,Hero> heroes;

    public static void main(String[] args) {
        GameEngine gameEngine = new GameEngine();
//        gameEngine.initialize();
//        gameEngine.doPickTurn();
//        gameEngine.doTurn();
    }

    public void initialize() {
        // todo initialize heros
    }

    public void doPickTurn(int firstHero , int secondHero) {
        try {
            firstPlayer.addHero((Hero) heroes.get(firstHero).clone());
            secondPlayer.addHero((Hero) heroes.get(secondHero).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void doTurn() {

    }
}
