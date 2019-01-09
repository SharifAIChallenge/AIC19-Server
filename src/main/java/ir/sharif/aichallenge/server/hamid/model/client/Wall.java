package ir.sharif.aichallenge.server.hamid.model.client;


import ir.sharif.aichallenge.server.hamid.model.Cell;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Wall {
    private int row;
    private int column;
}
