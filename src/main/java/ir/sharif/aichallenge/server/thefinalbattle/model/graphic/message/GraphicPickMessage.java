package ir.sharif.aichallenge.server.thefinalbattle.model.graphic.message;

import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.GraphicHero;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GraphicPickMessage {
    private GraphicHero[][] heroes;     //2D array, heroes[0] is for the first user and heroes[1] for the other
}
