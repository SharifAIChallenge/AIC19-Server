package ir.sharif.aichallenge.server.hamid.model.message;

import ir.sharif.aichallenge.server.hamid.model.Hero;
import lombok.*;

import java.util.List;

@Data
//@Builder
//@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class PicMessage {
    private List<Hero> myHeroes;
    private List<Hero> oppHeroes;
}
