package ir.sharif.aichallenge.server.thefinalbattle.controller;

import ir.sharif.aichallenge.server.thefinalbattle.model.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class OvertimeHandler {
    private GameEngine gameEngine;
    private Player behindPlayer;
    public static int MAX_DIFF_SCORE;
    public static final int INF_SCORE = 100000;
    private boolean arePlayersEqual = false;

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
            if (currentTurn.get() < maxTurns && players[0].getScore() < maxScore && players[1].getScore() < maxScore)
                return;

            if (Math.abs(players[0].getScore() - players[1].getScore()) >= MAX_DIFF_SCORE) {
                gameEngine.setMaxOvertime(0);
                gameEngine.setRemainingOvertime(0);
                return;
            }

            remainingOvertime = maxOvertime;
            maxTurns = currentTurn.get() + maxOvertime;
            maxScore = INF_SCORE;
            int score0 = players[0].getScore();
            int score1 = players[1].getScore();
            int totalUsedAp0 = players[0].getTotalUsedAp();
            int totalUsedAp1 = players[1].getTotalUsedAp();

            if (score0 > score1 || (score0 == score1 && totalUsedAp0 < totalUsedAp1)) {
                behindPlayer = players[1];
            }
            else if (score0 < score1 || (score0 == score1 && totalUsedAp0 > totalUsedAp1)) {
                behindPlayer = players[0];
            }
            else {
                //arePlayersEqual = true;
                maxOvertime -= 1;
                remainingOvertime = -1;
            }
        }
        else {      //behindPlayer is set
            /*if (arePlayersEqual) {
                remainingOvertime -= 1;
                if (remainingOvertime == 0) {
                    maxOvertime -= 1;
                    remainingOvertime = -1;
                }
                gameEngine.setMaxOvertime(maxOvertime);
                gameEngine.setRemainingOvertime(remainingOvertime);
                return;
            }*/

            if (Math.abs(players[0].getScore() - players[1].getScore()) >= MAX_DIFF_SCORE) {
                gameEngine.setMaxOvertime(0);
                gameEngine.setRemainingOvertime(0);
                return;
            }

            if (behindPlayer.getScore() > behindPlayer.getOpponent().getScore() ||
                    (behindPlayer.getScore() == behindPlayer.getOpponent().getScore() &&
                            behindPlayer.getTotalUsedAp() < behindPlayer.getOpponent().getTotalUsedAp())) {
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
