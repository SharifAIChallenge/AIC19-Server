package ir.sharif.aichallenge.server.thefinalbattle.model.message;

import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientAbilityConstants;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientHeroConstants;
import ir.sharif.aichallenge.server.thefinalbattle.model.client.ClientInitialMap;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class InitialMessage {
    private Map<String , Integer> gameConstants = new HashMap<>();
    private ClientInitialMap map;
    private List<ClientHeroConstants> heroConstants = new ArrayList<>();
    private List<ClientAbilityConstants> abilityConstants = new ArrayList<>();
}
