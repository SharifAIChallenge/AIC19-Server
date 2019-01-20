package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.hamid.model.ClientTurnMessage;
import ir.sharif.aichallenge.server.hamid.model.Hero;
import ir.sharif.aichallenge.server.hamid.model.Player;
import ir.sharif.aichallenge.server.hamid.model.enums.GameState;

import java.util.Map;
import java.util.Random;

public class GameEngine {

    public static final String PICK = "pick";
    private Player firstPlayer;
    private Player secondPlayer;
    private GameState state;
    private Map<Integer, Hero> heroes;

    public static void main(String[] args) {
        GameEngine gameEngine = new GameEngine();
//        gameEngine.initialize();
//        gameEngine.doPickTurn();
//        gameEngine.doTurn();
    }

    public void initialize() {
        // todo initialize heros
    }

    public void doPickTurn(int firstHero, int secondHero) {
        try {
            firstPlayer.addHero((Hero) heroes.get(firstHero).clone());
            secondPlayer.addHero((Hero) heroes.get(secondHero).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void doTurn(ClientTurnMessage message1, ClientTurnMessage message2) {
        if (state.equals(GameState.PICK)) {
            if (message1.getType().equals("pick") && message2.getType().equals("pick"))
                doPickTurn(message1.getHeroId(), message2.getHeroId());
            else if (message1.getType().equals("pick")) {
                doPickTurn(message1.getHeroId(), Math.abs(new Random().nextInt()) % heroes.size());//todo random or null
            } else {
                doPickTurn(Math.abs(new Random().nextInt()) % heroes.size(), message2.getHeroId());
            }
        }

        //move
        if (state.equals(GameState.MOVE)) {
            //todo sort
            //todo move and vision
        }

        //cast
        if (state.equals(GameState.CAST)) {
            //todo cast
        }


        //todo check game state

    }
}
