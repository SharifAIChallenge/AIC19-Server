package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.hamid.model.Player;

public class GameEngine {
    private Player player1, player2;

    public static void main(String[] args) {
        GameEngine gameEngine = new GameEngine();
        gameEngine.initialize();
        gameEngine.doPickTurn();
        gameEngine.doTurn();
    }

    public void initialize() {
        // todo
    }

    public void doPickTurn() {

    }

    public void doTurn() {

    }
}
