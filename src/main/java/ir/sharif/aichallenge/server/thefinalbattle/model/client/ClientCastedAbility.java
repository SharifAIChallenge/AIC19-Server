package ir.sharif.aichallenge.server.thefinalbattle.model.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClientCastedAbility {
    private int casterId;       //-1 if not in vision
    private List<Integer> targetHeroIds;   //not in list if not in vision
    private EmptyCell startCell;    //null for opponent if not in vision
    private EmptyCell endCell;      //null for opponent if not in vision
    private String abilityName;
}
