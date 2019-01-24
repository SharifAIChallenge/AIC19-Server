package ir.sharif.aichallenge.server.hamid.model;

import ir.sharif.aichallenge.server.hamid.model.enums.GameState;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class ClientTurnMessage {
    private GameState type;
    private List<Integer> heroesId;
    private List<Move> moves;
    private Integer heroId;
    private List<Cast> casts;
}
