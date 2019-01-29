package ir.sharif.aichallenge.server.hamid.model.message;

import ir.sharif.aichallenge.server.hamid.model.client.ClientAbilityConstants;
import ir.sharif.aichallenge.server.hamid.model.client.ClientHeroConstants;
import ir.sharif.aichallenge.server.hamid.model.client.ClientMap;
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
    private ClientMap map;
    private List<ClientHeroConstants> heroConstants = new ArrayList<>();
    private List<ClientAbilityConstants> abilityConstants = new ArrayList<>();
}
