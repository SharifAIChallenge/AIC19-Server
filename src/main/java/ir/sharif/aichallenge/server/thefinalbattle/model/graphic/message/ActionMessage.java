package ir.sharif.aichallenge.server.thefinalbattle.model.graphic.message;

import ir.sharif.aichallenge.server.thefinalbattle.model.graphic.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ActionMessage {
    private List<Action> actions;
}
