package ir.sharif.aichallenge.server.common.network.data;

import com.google.gson.JsonArray;
import ir.sharif.aichallenge.server.common.network.Json;

/**
 * Message class
 */
public class Message {

    public static final String NAME_PICK = "pick";
    public static final String NAME_TURN = "turn";
    public static final String NAME_MOVE = "move";
    public static final String NAME_ACTION = "action";
    public static final String NAME_INIT = "init";
    public static final String NAME_STATUS = "status";
    public static final String NAME_SHUTDOWN = "shutdown";
    public static final String NAME_END = "end";
    public static final String NAME_WRONG_TOKEN = "wrong token";

    public final String name;
    public final JsonArray args;

    public Message(String name, JsonArray args) {
        this.name = name;
        this.args = args;
    }

    public Message(String name, Object[] args) {
        this(name, Json.GSON.toJsonTree(args).getAsJsonArray());
    }

//    public void setName(String name) {
//        this.name = name;
//    }

//    public void setArgs(Object[] args) {
//        this.args = args;
//    }

}
