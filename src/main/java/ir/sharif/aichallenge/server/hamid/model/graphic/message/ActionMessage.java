package ir.sharif.aichallenge.server.hamid.model.graphic.message;

import ir.sharif.aichallenge.server.hamid.model.graphic.Action;
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
