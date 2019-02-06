package ir.sharif.aichallenge.server.hamid.model.graphic.message;

import lombok.Setter;

@Setter
public class EndMessage {   //todo set this
    private int[] scores;
    private int[] usedAPs;
    private int winner;     // -1 if draw
}
