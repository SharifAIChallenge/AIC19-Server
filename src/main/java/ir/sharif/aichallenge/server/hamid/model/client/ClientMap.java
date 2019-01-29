package ir.sharif.aichallenge.server.hamid.model.client;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClientMap {
    private int rowNum;
    private int columnNum;
    private ClientCell[][] cells;
}
