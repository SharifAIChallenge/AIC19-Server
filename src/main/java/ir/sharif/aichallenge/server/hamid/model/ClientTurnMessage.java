package ir.sharif.aichallenge.server.hamid.model;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class ClientTurnMessage {
    private String type;
    private List<Integer> herosId;
    private List<Move> moves;
    private Integer heroId;
    private String abilityName;
    private int targetRow;
    private int targetCollumn;
}
