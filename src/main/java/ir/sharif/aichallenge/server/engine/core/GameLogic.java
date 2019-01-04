package ir.sharif.aichallenge.server.engine.core;

import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.engine.config.IntegerParam;

/**
 * The abstract class representing the main game logic of the user's game.
 * <p>
 * This class will be the simulator engine of the game.
 * </p>
 */
public interface GameLogic {

    IntegerParam currentTurn = new IntegerParam("CurrentTurn", -1);
    /**
     * Returns number of players.
     *
     * @return number of players
     */
    public int getClientsNum();

    public long getClientResponseTimeout();

    public long getTurnTimeout();

    /**
     * This method must send initial and necessary values to UI and clients.
     */
    public void init();

    /**
     * @return UI initial message
     */
    public Message getUIInitialMessage();

    /**
     * @return Client initial message
     */
    public Message[] getClientInitialMessages();

    /**
     * Simulate events based on the current turn event and calculate the changes in game.
     *
     * @param environmentEvent Events that is related to environment. Suppose we want to develop a strategic game.
     *                         Increasing/Decreasing a specific resource in map is an environment event.
     * @param clientsEvent     Events that is related to client e.g. moving the player.
     */
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent);

    /**
     * This method generates the output based on the changes that were calculated in
     * {@link #simulateEvents}.
     */
    public void generateOutputs();

    public Message getUIMessage();

    public Message getStatusMessage();

    public Message[] getClientMessages();

    /**
     * This method is used for making the environment events.
     *
     * @return An array that is environment events.
     */
    public Event[] makeEnvironmentEvents();

    public boolean isGameFinished();

    public void terminate();
}
