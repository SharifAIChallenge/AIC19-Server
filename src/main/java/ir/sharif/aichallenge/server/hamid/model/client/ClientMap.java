package ir.sharif.aichallenge.server.hamid.model.client;

import lombok.*;

@Builder
@Getter
@Setter
public class ClientMap {
    private int rowNum;
    private int columnNum;
    private ClientCell[][] cells;

    public ClientMap(int rowNum, int columnNum) {
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        cells = new ClientCell[rowNum][columnNum];
    }
}
