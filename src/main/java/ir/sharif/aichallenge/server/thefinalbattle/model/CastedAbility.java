package ir.sharif.aichallenge.server.thefinalbattle.model;

import com.google.gson.JsonObject;
import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.thefinalbattle.model.ability.Ability;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CastedAbility {
    private Hero casterHero;
    private List<Hero> targetHeroes;
    private Cell startCell;
    private Cell endCell;
    private Ability ability;

    public JsonObject getJsonObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("casterId", casterHero.getId());
        ArrayList<Integer> targetHeroIds = new ArrayList<>();
        for (Hero targetHero : targetHeroes)
        {
            targetHeroIds.add(targetHero.getId());
        }
        object.add("targetHeroIds", Json.GSON.toJsonTree(targetHeroIds).getAsJsonArray());
        object.add("startCell", Json.GSON.toJsonTree(startCell.getEmptyCell()));
        object.add("endCell", Json.GSON.toJsonTree(endCell.getEmptyCell()));
        object.addProperty("abilityName", ability.getName());
        return object;
    }
}
