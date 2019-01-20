package ir.sharif.aichallenge.server.hamid.model.ability;

import lombok.*;

@Data
//@Builder
//@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class DodgeAbility extends Ability {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
