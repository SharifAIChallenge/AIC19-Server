package ir.sharif.aichallenge.server.hamid.model.graphic.message;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MoveMessage {
    private String movements;   // example "udlrnnud"
    private int[] currentAP;    //currentAP[0], currentAP[1]
}
