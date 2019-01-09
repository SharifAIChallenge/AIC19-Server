package ir.sharif.aichallenge.server.hamid.controller;

import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.engine.core.GameLogic;

import java.util.concurrent.atomic.AtomicInteger;

public class GameHandler implements GameLogic {

    public static final int CLIENT_NUM = 2;
    public static final int CLIENT_RESPONSE_TIME = 0;
    public static final int TURN_TIMEOUT = 0;

    private AtomicInteger currentTrun;

    @Override
    public int getClientsNum() {
        return CLIENT_NUM;
    }

    @Override
    public long getClientResponseTimeout() {
        return CLIENT_RESPONSE_TIME;
    }

    @Override
    public long getTurnTimeout() {
        return TURN_TIMEOUT;
    }

    @Override
    public void init() {
        //todo read from map
    }

    @Override
    public Message getUIInitialMessage() {
        return null;
    }//for ui

    @Override
    public Message[] getClientInitialMessages() {
        return new Message[0];
    }//for clients

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
