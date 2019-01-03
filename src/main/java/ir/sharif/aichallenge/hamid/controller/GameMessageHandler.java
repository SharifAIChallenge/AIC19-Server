package ir.sharif.aichallenge.hamid.controller;

import ir.sharif.aichallenge.common.model.Event;
import ir.sharif.aichallenge.common.network.data.Message;
import ir.sharif.aichallenge.server.server.core.GameLogic;

public class GameMessageHandler implements GameLogic {
    @Override
    public int getClientsNum() {
        return 0;
    }

    @Override
    public long getClientResponseTimeout() {
        return 0;
    }

    @Override
    public long getTurnTimeout() {
        return 0;
    }

    @Override
    public void init() {

    }

    @Override
    public Message getUIInitialMessage() {
        return null;
    }

    @Override
    public Message[] getClientInitialMessages() {
        return new Message[0];
    }

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {

    }

    @Override
    public void generateOutputs() {

    }

    @Override
    public Message getUIMessage() {
        return null;
    }

    @Override
    public Message getStatusMessage() {
        return null;
    }

    @Override
    public Message[] getClientMessages() {
        return new Message[0];
    }

    @Override
    public Event[] makeEnvironmentEvents() {
        return new Event[0];
    }

    @Override
    public boolean isGameFinished() {
        return false;
    }

    @Override
    public void terminate() {

    }
}
