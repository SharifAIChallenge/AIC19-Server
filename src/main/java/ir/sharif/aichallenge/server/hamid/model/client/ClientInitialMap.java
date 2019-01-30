package ir.sharif.aichallenge.server.hamid.model.client;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class ClientInitialMap
{
    private int rowNum;
    private int columnNum;
    private ClientInitialCell[][] cells;
}
