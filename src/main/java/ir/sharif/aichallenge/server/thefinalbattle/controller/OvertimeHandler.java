package ir.sharif.aichallenge.server.thefinalbattle.controller;

import ir.sharif.aichallenge.server.thefinalbattle.model.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class OvertimeHandler {
    private GameEngine gameEngine;
    private Player behindPlayer;
    public static final int MAX_DIFF_SCORE = 30;    //todo set this
    public static final int INF_SCORE = 100000;

    public OvertimeHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void updateOvertime() {
        AtomicInteger currentTurn = gameEngine.getCurrentTurn();
        Player[] players = gameEngine.getPlayers();
        int maxOvertime = gameEngine.getMaxOvertime();
        int remainingOvertime = gameEngine.getRemainingOvertime();
        int maxTurns = gameEngine.getMaxTurns();
        int maxScore = gameEngine.getMaxScore();

        if (maxOvertime == 0 || remainingOvertime == 0)     // game must be finished
            return;

        if (remainingOvertime == -1) {
            if (currentTurn.get() <= maxTurns && players[0].getScore() < maxScore && players[1].getScore() < maxScore)
                return;

            if (Math.abs(players[0].getScore() - players[1].getScore()) >= MAX_DIFF_SCORE) {
                gameEngine.setMaxOvertime(0);
                gameEngine.setRemainingOvertime(0);
                return;
            }

            remainingOvertime = maxOvertime;
            maxTurns = currentTurn.get() + maxOvertime;
            maxScore = INF_SCORE;
            if (players[0].getScore() > players[1].getScore()) {
                behindPlayer = players[1];
            }
            else if (players[0].getScore() < players[1].getScore()) {
                behindPlayer = players[0];
            }
            else {      //todo used ap (better not to be)
                maxOvertime -= 1;
                remainingOvertime = -1;
            }
        }
        else {      //behindPlayer is set
            if (Math.abs(players[0].getScore() - players[1].getScore()) >= MAX_DIFF_SCORE) {
                gameEngine.setMaxOvertime(0);
                gameEngine.setRemainingOvertime(0);
                return;
            }    //todo whenever difference gets more than a limit game ends ok?

            if (behindPlayer.getScore() > behindPlayer.getOpponent().getScore()) {  //todo used ap (better not to be)
                maxOvertime -= 1;
                remainingOvertime = maxOvertime;
                behindPlayer = behindPlayer.getOpponent();
            }
            else {
                remainingOvertime -= 1;
            }
        }

        gameEngine.setMaxOvertime(maxOvertime);
        gameEngine.setRemainingOvertime(remainingOvertime);
        gameEngine.setMaxTurns(maxTurns);
        gameEngine.setMaxScore(maxScore);
    }
}
