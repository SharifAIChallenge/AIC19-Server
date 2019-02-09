package ir.sharif.aichallenge.server.thefinalbattle.model.client;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ClientCell {
    private boolean isWall;
    private boolean isInMyRespawnZone;
    private boolean isInOppRespawnZone;
    private boolean isInObjectiveZone;
    private boolean isInVision;
    private int row;
    private int column;
}
