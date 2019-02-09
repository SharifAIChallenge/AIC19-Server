package ir.sharif.aichallenge.server.thefinalbattle.model.client;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ClientInitialCell {
    private boolean isWall;
    private boolean isInFirstRespawnZone;
    private boolean isInSecondRespawnZone;
    private boolean isInObjectiveZone;
    private int row;
    private int column;
}