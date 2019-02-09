package ir.sharif.aichallenge.server.thefinalbattle.model.client;

import ir.sharif.aichallenge.server.thefinalbattle.model.Cell;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmptyCell {
    int row;
    int column;

    public EmptyCell(Cell cell) {
        this(cell.getRow(), cell.getColumn());
    }
}
