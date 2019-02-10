package ir.sharif.aichallenge.server.engine.network;

import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.Json;
import ir.sharif.aichallenge.server.common.network.JsonSocket;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.common.util.Log;
import ir.sharif.aichallenge.server.engine.config.Configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ClientNetwork} is a engine which is responsible for
 * sending messages from engine to players
 * and receiving players' requests (messages).
 * <p>
 * First of all clients (players) must be defined to the engine via method
 * {@link #defineClient}. Server assigns an ID to each client so further calls
 * get ID of the client instead of its token.
 * <p>
 * When a client (player) is connected to the engine, it sends a token and waits
 * for the initial message which contains necessary data of beginning of the
 * game.
 * <p>
 * Each turn engine sends some data to the clients, these data are first queued
 * by {@link #queue} and then sent by send method. Server does it best to send
 * messages simultaneously to clients, i.e. sending procedure is fair.
 * <p>
 * The communications are one sided, i.e. everything which is sent by client is
 * ignored by the engine.
 */
public class ClientNetwork extends NetServer {
    // Log tag
    private static final String TAG = "ClientNetwork";

    // Indicates that receive time is valid or not
    private volatile boolean receiveTimeFlag;

    // Mapping of tokens to IDs
    private HashMap<String, ArrayList<Integer>> mTokens;

    // Client handlers
    private ArrayList<ClientHandler> mClients;

    // A thread pool used to send all messages
    private ExecutorService sendExecutor;

    // A thread pool used to receive messages from clients
    private ExecutorService receiveExecutor;

    // A thread pool used to accept and verify clients
    private ExecutorService acceptExecutor;

    //Simulate Thread Semaphore
    private Semaphore simulationSemaphore;

    //Current Turn of game
    private AtomicInteger currentTurn;

    //Current move phase of game
    private AtomicInteger currentMovePhase;

    //End flags for clients
    private ArrayList<AtomicBoolean> endReceivedFlags;

    /**
     * Constructor
     */
    public ClientNetwork() {
        mTokens = new HashMap<>();
        mClients = new ArrayList<>();
        endReceivedFlags = new ArrayList<>();
        sendExecutor = Executors.newCachedThreadPool();
        receiveExecutor = Executors.newCachedThreadPool();
    }

    public ClientNetwork(Semaphore simulationSemaphore, AtomicInteger currentTurn, AtomicInteger currentMovePhase) {
        mTokens = new HashMap<>();
        mClients = new ArrayList<>();
        endReceivedFlags = new ArrayList<>();
        sendExecutor = Executors.newCachedThreadPool();
        receiveExecutor = Executors.newCachedThreadPool();

        this.simulationSemaphore = simulationSemaphore;
        this.currentTurn = currentTurn;
        this.currentMovePhase = currentMovePhase;
    }

    /**
     * Defines a client with a token. It actually assigns an ID and a handler to
     * the client.
     *
     * @param token token of the client
     * @return ID of the client
     * @see {@link #omitAllClients}
     */
    public int defineClient(String token) {
        if (isListening())
            throw new RuntimeException("Server is not terminated when defineClient() is called.");
        int id = mClients.size();
        if (Configs.PARAM_AIC_DEPLOY.getValue()) {
            if (mTokens.containsKey(token))
                throw new RuntimeException("Duplicate token. " + token);
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(id);
            mTokens.put(token, arr);
            mClients.add(newClient());
        } else {
            ArrayList<Integer> arr = mTokens.get(token);
            if (arr == null)
                arr = new ArrayList<>();
            arr.add(id);
            mTokens.put(token, arr);
            mClients.add(newClient());
        }
        return id;
    }

    /**
     * Creates a new handler.
     *
     * @return new handler
     */
    private ClientHandler newClient() {
        AtomicBoolean endReceivedFlag = new AtomicBoolean(false);
        ClientHandler client = new ClientHandler(simulationSemaphore, currentTurn, currentMovePhase, endReceivedFlag);
        sendExecutor.submit(client.getSender());
        endReceivedFlags.add(endReceivedFlag);
        return client;
    }

    public boolean timeValidator() {
        return receiveTimeFlag;
    }

    /**
     * Remove defined clients and free memory allocated for them.
     *
     * @see {@link #defineClient}
     */
    public void omitAllClients() {
        if (isListening())
            throw new RuntimeException("Server is not terminated when omitAllClients() is called.");
        mClients.forEach(ClientHandler::terminate);
        mTokens.clear();
        mClients.clear();
        sendExecutor.shutdownNow();
        receiveExecutor.shutdownNow();
        System.gc();
        mTokens = new HashMap<>();
        mClients = new ArrayList<>();
        sendExecutor = Executors.newCachedThreadPool();
        receiveExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Queues a message for a client.
     *
     * @param clientID ID of the client
     * @param msg      message
     * @see {@link #defineClient}
     * @see {@link #sendAllBlocking}
     */
    public void queue(int clientID, Message msg) {
        mClients.get(clientID).queue(msg);
    }

    /**
     * Sends all queued messages. Method will not return until all messages
     * sent. (or some failure occur)
     *
     * @see {@link #queue}
     */
    public void sendAllBlocking() {
        CyclicBarrier sendBarrier = new CyclicBarrier(mClients.size() + 1);
        for (ClientHandler client : mClients) {
            sendExecutor.submit(() -> {
                try {
                    sendBarrier.await();
                    client.send();
                    sendBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    Log.d(TAG, "waiting barrier interrupted.", e);
                }
            });
        }
        try {
            sendBarrier.await(); // start sending
            sendBarrier.await(); // wait to send
        } catch (Exception e) {
            Log.d(TAG, "waiting barrier interrupted.", e);
        }
    }

    /**
     * Stops receiving messages from clients. Any message which is arrived after
     * a call of this method is discarded by the handler.
     *
     * @see {@link #startReceivingAll}
     */
    public void stopReceivingAll() {
        receiveTimeFlag = false;
        endReceivedFlags.get(0).set(true);
        endReceivedFlags.get(1).set(true);
    }

    /**
     * Starts receiving messages from clients. The last message which is arrived
     * after a call of this method and before the corresponding call of
     * {@link #stopReceivingAll} is stored as "last valid message".
     *
     * @see {@link #stopReceivingAll}
     */
    public void startReceivingAll() {
        receiveTimeFlag = true;
        endReceivedFlags.get(0).set(false);
        endReceivedFlags.get(1).set(false);
    }

    /**
     * Returns last valid message which is received from a client.
     *
     * @param clientID ID of the client
     * @return last valid message or <code>null</code> if there is no valid msg
     * @see {@link #defineClient}
     */
    public Message[] getReceivedMessages(int clientID) {
        return mClients.get(clientID).getReceivedMessages();
    }

    /**
     * Returns last valid event which is received from a client.
     *
     * @param clientID ID of the client
     * @return last valid event or <code>null</code> if there is no valid event
     * @see {@link #getReceivedMessages}
     */
    public Event[] getReceivedEvents(int clientID) {
        Message[] messages = getReceivedMessages(clientID);
        ArrayList<Event> allEvents = new ArrayList<>();
        for (Message msg : messages) {
            try {
            /*JsonArray eventArray = ((JsonElement)msg.args[0]).getAsJsonArray();
            events = new Event[eventArray.size()];
            for (int i = 0; i < events.length; i++)
                events[i] = gson.fromJson(eventArray.get(i), Event.class);*/
                int size = msg.args.size();
                for (int i = 0; i < size; i++) {
                    allEvents.add(Json.GSON.fromJson(msg.args.get(i), Event.class));
                }
            } catch (Exception e) {
                Log.i(TAG, "Error getting received messages.", e);
            }
        }
        return allEvents.toArray(new Event[allEvents.size()]);
    }

    @Override
    protected void accept(JsonSocket client) {
        acceptExecutor.submit(() -> {
            try {
                verifyClient(client);
                Log.i(TAG, "Client accepted.");
            } catch (Exception e) {
                // if anything went wrong reject the client!
                Log.w(TAG, "Client rejected.", e);
                try {
                    client.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void verifyClient(JsonSocket client) throws Exception {
        // get the token, timeout is 2000 seconds
        Future<Message> futureMessage
                = acceptExecutor.submit(() -> client.get(Message.class));
        Message token = futureMessage.get(2000, TimeUnit.SECONDS);
        // check the token
        if (token != null && "token".equals(token.name) && token.args != null
                && token.args.size() >= 1) {
            String clientToken = token.args.get(0).getAsString();
            ArrayList<Integer> ids = mTokens.get(clientToken);
            if (ids != null) {
                for (int clientID : ids) {
                    ClientHandler clientHandler = mClients.get(clientID);
                    if (!clientHandler.isConnected()) {
                        clientHandler.bind(client);
                        Runnable receiver = clientHandler.getReceiver(() -> receiveTimeFlag);
                        receiveExecutor.submit(receiver);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Blocks caller method until the specified client send a message.
     *
     * @param clientID ID of the client
     * @throws InterruptedException if current thread is interrupted.
     * @see {@link #defineClient}
     */
    public void waitForClientMessage(int clientID) throws InterruptedException {
        mClients.get(clientID).waitForClientMessage();
    }

    /**
     * Blocks caller method at most <code>timeout</code> milliseconds until
     * the specified client send a message.
     *
     * @param clientID ID of the client
     * @throws InterruptedException if current thread is interrupted.
     * @see {@link #defineClient}
     */
    public void waitForClientMessage(int clientID, long timeout) throws InterruptedException {
        mClients.get(clientID).waitForClientMessage(timeout);
    }

    /**
     * Blocks caller method until the specified client is connected to the engine.
     *
     * @param clientID ID of the client
     * @throws InterruptedException if current thread is interrupted.
     * @see {@link #defineClient}
     */
    public void waitForClient(int clientID) throws InterruptedException {
        mClients.get(clientID).waitForClient();
    }

    /**
     * Blocks caller method at most <code>timeout</code> milliseconds until
     * the specified client is connected to the engine.
     *
     * @param clientID ID of the client
     * @param timeout  timeout in milliseconds
     * @throws InterruptedException if current thread is interrupted.
     * @see {@link #defineClient}
     */
    public void waitForClient(int clientID, long timeout) throws InterruptedException {
        mClients.get(clientID).waitForClient(timeout);
    }

    /**
     * Blocks caller method until all clients are connected to the engine.
     *
     * @throws InterruptedException if current thread is interrupted.
     */
    public void waitForAllClients() throws InterruptedException {
        for (ClientHandler client : mClients)
            client.waitForClient();
    }

    /**
     * Blocks caller method at most <code>timeout</code> milliseconds until all
     * clients are connected to the engine.
     *
     * @param timeout timeout in milliseconds
     * @throws InterruptedException if current thread is interrupted.
     */
    public void waitForAllClients(long timeout) throws InterruptedException {
        for (ClientHandler client : mClients) {
            long start = System.currentTimeMillis();
            client.waitForClient(timeout);
            long end = System.currentTimeMillis();
            timeout -= end - start;
            if (timeout <= 0)
                return;
        }
    }

    /**
     * Returns number of connected clients.
     *
     * @return num of connected clients.
     */
    public int getNumberOfConnected() {
        int noc = 0;
        for (ClientHandler client : mClients)
            if (client.isConnected())
                noc++;
        return noc;
    }

    /**
     * States that a client is connected or not.
     *
     * @param clientID ID of the client
     * @return true if the client is connected
     * @see {@link #defineClient}
     */
    public boolean isConnected(int clientID) {
        return mClients.get(clientID).isConnected();
    }

    @Override
    public void listen(int port) {
        Log.i(TAG, "Listening on port " + port + ".");
        super.listen(port);
        acceptExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void terminate() {
        super.terminate();
        acceptExecutor.shutdownNow();
        receiveExecutor.shutdownNow();
        sendExecutor.shutdownNow();
    }

    public void shutdownAll() {
        mClients.forEach(ClientHandler::terminateReceiving);
        Message shutdown = new Message(Message.NAME_SHUTDOWN, new Object[]{});
        mClients.forEach(c -> c.queue(shutdown));
        sendAllBlocking();
        mClients.forEach(ClientHandler::terminateSending);
    }
}
