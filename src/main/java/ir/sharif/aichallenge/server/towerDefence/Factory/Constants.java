package ir.sharif.aichallenge.server.towerDefence.Factory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * Created by msi1 on 1/26/2018.
 */
public class Constants
{
    public static int INIT_HEALTH;
    public static int INIT_MONEY;
    public static int NUMBER_OF_TURNS;
    public static int NUMBER_OF_BEANS;
    public static int NUMBER_OF_NUKES;
    public static int RANGE_OF_NUKES;
    public static double[][] UNITS_CONSTANTS = new double[2][11];
    public static double[][] TOWERS_CONSTANTS = new double[2][9];
    private static JsonArray CONSTANTS_JSON;

    public static void setConsts(JsonArray consts)
    {
        Gson gson = new Gson();
        CONSTANTS_JSON = consts;
        INIT_HEALTH = consts.get(0).getAsInt();
        INIT_MONEY = consts.get(1).getAsInt();
        NUMBER_OF_TURNS = consts.get(2).getAsInt();
        NUMBER_OF_BEANS = consts.get(3).getAsInt();
        NUMBER_OF_NUKES = consts.get(4).getAsInt();
        RANGE_OF_NUKES = consts.get(5).getAsInt();
        UNITS_CONSTANTS = gson.fromJson(consts.get(6).getAsJsonArray(), double[][].class);
        TOWERS_CONSTANTS = gson.fromJson(consts.get(7).getAsJsonArray(), double[][].class);
    }

    public static JsonArray getConstantsJson()
    {
        return CONSTANTS_JSON;
    }
}
