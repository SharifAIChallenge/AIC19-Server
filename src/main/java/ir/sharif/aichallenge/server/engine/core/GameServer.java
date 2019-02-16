package ir.sharif.aichallenge.server.engine.core;

//import Swarm.SwarmGameLogic;

import ir.sharif.aichallenge.server.common.model.Event;
import ir.sharif.aichallenge.server.common.network.data.Message;
import ir.sharif.aichallenge.server.common.util.Log;
import ir.sharif.aichallenge.server.engine.network.ClientNetwork;
import ir.sharif.aichallenge.server.engine.network.UINetwork;
import ir.sharif.aichallenge.server.engine.config.ClientConfig;
import ir.sharif.aichallenge.server.engine.config.Configs;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core controller of the framework, controls the {@link GameLogic GameLogic}, Swarm.main loop of the game and
 * does the output controlling operations.
 * <p>
 * This class runs the Swarm.main running thread of the framework. Class interacts with the clients, UI, and the
 * GameLogic itself.
 * Threads in this class, will gather the clients' events
 * (See also {@link ClientNetwork ClientNetwork}), send them to the Swarm.main Game
 * (See also {@link GameLogic GameLogic})
 * The output will be manipulated and sent to the appropriate controller within a inner module of the class
 * (OutputController).
 * The sequence of the creation and running the operations of this class will be through the call of the following
 * methods.
 * {@link GameServer#start() start()} and then at the
 * moment the external terminal user wants to shut down the games loop (except than waiting for the
 * {@link GameLogic GameLogic} to flag the end of the game), the
 * {@link GameServer#shutdown() shutdown()} method would be called.
 * Note that shutting down the {@link GameServer GameServer} will not immediately stop the threads,
 * actually it will set a shut down request flag in the class, which will closes the thread in the aspect of
 * accepting more inputs, and the terminate the threads as soon as the operation queue got empty.
 * </p>
 */
public class GameServer {
    private final int mClientsNum;
    private ClientNetwork mClientNetwork;
    private UINetwork mUINetwork;
    private GameLogic mGameLogic;
    private OutputController mOutputController;
    private ClientConfig[] mClientConfigs;

    private Loop mLoop;

    private Semaphore serverSemaphore;
    private Semaphore simulationSemaphore;


    /**
     * Constructor of the {@link GameServer GameServer}, connects the handler to the Clients through
     * {@link ClientNetwork ClientNetwork} and to the UI through
     * {@link UINetwork UINetwork}.
     * <p>
     * The constructor accepts the instances of {@link GameServer GameServer} and
     * {@link ClientNetwork ClientNetwork} classes. Then sets some configurations of the loops
     * within the "turn_timeout.conf" file ({@see https://github.com/JavaChallenge/JGFramework/wiki wiki}).
     * </p>
     */
    public GameServer(GameLogic gameLogic, String[] cmdArgs) {
        Configs.handleCMDArgs(cmdArgs);
        mGameLogic = gameLogic;
        mGameLogic.init();
        mClientsNum = mGameLogic.getClientsNum();
        setClientConfigs();

        mClientNetwork = new ClientNetwork();

        mUINetwork = new UINetwork();
        mOutputController = new OutputController(mUINetwork);
        initGame();
    }

    public GameServer(GameLogic gameLogic, String[] cmdArgs, AtomicInteger currentTurn,
                      AtomicInteger currentMovePhase) {
        Configs.handleCMDArgs(cmdArgs);
        mGameLogic = gameLogic;
        mGameLogic.init();
        mClientsNum = mGameLogic.getClientsNum();
        setClientConfigs();

        serverSemaphore = new Semaphore(0);
        simulationSemaphore = new Semaphore(0);
        mClientNetwork = new ClientNetwork(serverSemaphore, currentTurn, currentMovePhase);

        mUINetwork = new UINetwork();
        mOutputController = new OutputController(mUINetwork);
        initGame();
    }

    private void setClientConfigs() {
        mClientConfigs = new ClientConfig[mClientsNum];
        for (int i = 0; i < mClientsNum; i++) {
            mClientConfigs[i] = new ClientConfig();
            Configs.CLIENT_CONFIGS.add(mClientConfigs[i]);
        }
    }

    private void initGame() {
        for (int i = 0; i < mClientsNum; ++i) {
            int id = mClientNetwork.defineClient(mClientConfigs[i].getToken());
            if (id != i) {
                throw new RuntimeException("Client ID and client order does not match");
            }
            mClientConfigs[i].setID(id);
        }

        if (Configs.PARAM_UI_ENABLE.getValue() == Boolean.TRUE) {
            mUINetwork.listen(Configs.PARAM_UI_PORT.getValue());
            mClientNetwork.listen(Configs.PARAM_CLIENTS_PORT.getValue());

            try {
                System.err.println("Waiting for UI for " + Configs.PARAM_UI_CONNECTIONS_TIMEOUT.getValue() + "ms.");
                mUINetwork.waitForClient(Configs.PARAM_UI_CONNECTIONS_TIMEOUT.getValue());
                System.err.println("UI connected.");
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for ui interrupted");
            }

            try {
                mClientNetwork.waitForAllClients(Configs.PARAM_CLIENTS_CONNECTIONS_TIMEOUT.getValue());
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for clients interrupted");
            }

            System.err.println("Sending first UI message.");
            Message initialMessage = mGameLogic.getUIInitialMessage();
            mOutputController.putMessage(initialMessage);
            try {
                mOutputController.waitToSend();
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for ui interrupted");
            }

            // Uncomment this if you want to send init and first turn message at the same time
//            Message[] initialMessages = mGameLogic.getClientInitialMessages();
//            for (int i = 0; i < initialMessages.length; ++i) {
//                mClientNetwork.queue(i, initialMessages[i]);
//            }
//            mClientNetwork.sendAllBlocking();
        } else {
            mClientNetwork.listen(Configs.PARAM_CLIENTS_PORT.getValue());

            try {
                mClientNetwork.waitForAllClients(Configs.PARAM_CLIENTS_CONNECTIONS_TIMEOUT.getValue());
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for clients interrupted");
            }

            // Uncomment this if you want to send init and first turn message at the same time
//            Message[] initialMessages = mGameLogic.getClientInitialMessages();
//            for (int i = 0; i < initialMessages.length; ++i) {
//                mClientNetwork.queue(i, initialMessages[i]);
//            }
//            mClientNetwork.sendAllBlocking();
        }
    }

    public void waitForClients() throws InterruptedException {
        mClientNetwork.waitForAllClients();
    }

    /**
     * Starts the Swarm.main game ({@link GameLogic GameLogic}) loop and the
     * {@link OutputController OutputController} operations in two new {@link Thread Thread}.
     */
    public void start() {
        mLoop = new Loop();
        new Thread(mLoop).start();
    }

    /**
     * Registers a shutdown request into the Swarm.main loop and {@link OutputController OutputController} class
     * <p>
     * Note that the shutdown requests, will be responded as soon as the current queue of operations got freed.
     * </p>
     */
    public void shutdown() {
        if (mLoop != null)
            mLoop.shutdown();
        if (mOutputController != null)
            mOutputController.shutdown();
    }

    public void waitForFinish() throws InterruptedException {
        final Loop loop = mLoop;
        if (loop != null)
            synchronized (loop) {
                loop.wait();
            }
    }

    private void err(String title, Throwable exception) {
        System.err.println(title + " failed with message " + exception.getMessage() + ", stack: " + Arrays.toString(exception.getStackTrace()));
    }

    /**
     * In order to give the loop a thread to be ran beside of the Swarm.main loop.
     * <p>
     * This inner class has a {@link java.util.concurrent.Callable Callable} part, which is wrote down as a
     * runnable code template. This template is composed by the multiple steps in every turn of the game.
     * </p>
     */
    /*
    this is main
     */
    private class Loop implements Runnable {

        private boolean shutdownRequest = false;

        private Event[] environmentEvents;
        private Event[][] clientEvents;

        /**
         * The run method of the {@link Runnable Run    nable} interface which will create a
         * {@link java.util.concurrent.Callable Callable} instance and call it in a while until the finish flag if the
         * game had been raised or the shutdown request sent to the class (through
         * {@link Loop#shutdown() shutdown()} method)
         */
        @Override
        public void run() {
            clientEvents = new Event[mClientsNum][];
            for (int i = 0; i < clientEvents.length; i++) {
                clientEvents[i] = new Event[0];
            }

            Runnable simulate = () -> {
//                try {
//                    mGameLogic.simulateEvents(environmentEvents, clientEvents);
//                } catch (Exception e) {
//                    err("Simulation", e);
//                }
//                try {
//                    mGameLogic.generateOutputs();
//                } catch (Exception e) {
//                    err("Generating outputs", e);
//                }

//                mOutputController.putMessage(mGameLogic.getUIMessage());
//                mOutputController.putMessage(mGameLogic.getStatusMessage());

                Message[] output = mGameLogic.getClientMessages();
//                mOutputController.putMessage(mGameLogic.getStatusMessage());
                for (int i = 0; i < output.length; ++i) {
                    mClientNetwork.queue(i, output[i]);
                }

//                if (mGameLogic.isGameFinished()) {
//                    mClientNetwork.sendAllBlocking();
//                    mGameLogic.terminate();
//                    mClientNetwork.shutdownAll();
//                    mLoop.shutdown();
//                    mOutputController.shutdown();
//                    return;
//                }


//                long elapsedTime = System.currentTimeMillis();
//                environmentEvents = mGameLogic.makeEnvironmentEvents();
//                elapsedTime = System.currentTimeMillis() - elapsedTime;

//                if (timeout - elapsedTime > 0) {
//                    try {
//                        Thread.sleep(timeout - elapsedTime);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Waiting for clients interrupted");
//                    }
//                }

                mClientNetwork.startReceivingAll();
                mClientNetwork.sendAllBlocking();
                long timeout = mGameLogic.getClientResponseTimeout();
                long t1 = System.currentTimeMillis();
                try {
                    serverSemaphore.tryAcquire(2, timeout, TimeUnit.MILLISECONDS);
                    serverSemaphore.drainPermits();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long t2 = System.currentTimeMillis();
                System.err.println(t2 - t1 + " time went by with timeout " + timeout);
                mClientNetwork.stopReceivingAll();


                clientEvents = new Event[mClientsNum][];
                for (int i = 0; i < mClientsNum; ++i) {
                    Event[] events = mClientNetwork.getReceivedEvents(i);
                    clientEvents[i] = events;
                }

                try {
                    mGameLogic.simulateEvents(environmentEvents, clientEvents);
//                    mOutputController.putMessage(mGameLogic.getUIMessage()); TODO don't forget these
//                    mOutputController.putMessage(mGameLogic.getStatusMessage());
                } catch (Exception e) {
                    err("Simulation", e);
                    e.printStackTrace();
                }

                if (mGameLogic.isGameFinished()) {
                    try {
                        mGameLogic.generateOutputs(); // added at AIC 2019
                        mGameLogic.terminate();
//                        Message shutdown = new Message(Message.NAME_SHUTDOWN, new Object[]{});
//                        for (int i = 0; i < mClientsNum; i++) {
//                            mClientNetwork.queue(i, shutdown);
//                        }
//                        mClientNetwork.sendAllBlocking();
                        mClientNetwork.shutdownAll();
                        Thread.sleep(1000);
                        mClientNetwork.terminate();
                        Message uiShutdown = new Message(Message.NAME_SHUTDOWN, new Object[]{});
                        mOutputController.putMessage(uiShutdown);
                        mOutputController.waitToSend();
                        mLoop.shutdown();
                        mOutputController.shutdown();
                        mUINetwork.terminate();
                    } catch (Exception e) {
                        err("Finishing game", e);
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // TODO add params to game logic
//                    if(SwarmGameLogic.PARAM_SHOW_DEBUG_UI.equals(false)) {
                    System.exit(0);
//                    }
                    return;
                }
                simulationSemaphore.release();
            };

            while (!shutdownRequest) {
                long start = System.currentTimeMillis();
                try {
                    simulate.run();
//                    System.err.println("Before Acquire() function");
                    simulationSemaphore.acquire();
//                    System.err.println("After Acquire() function");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long end = System.currentTimeMillis();
                long remaining = mGameLogic.getTurnTimeout() - (end - start);
                if (remaining <= 0) {
                    Log.i("GameServer", "Simulation timeout passed!");
                } /*else {
                    try {
                        Thread.sleep(remaining);
                    } catch (InterruptedException e) {
                        Log.i("GameServer", "Loop interrupted!");
                        break;
                    }
                }*/
            }

            synchronized (this) {
                notifyAll();
            }
        }

        /**
         * Will set the shutdown request flag in order to finish the Swarm.main {@link Loop Loop} at
         * the first possible turn
         */
        public void shutdown() {
            this.shutdownRequest = true;
        }
    }

}
