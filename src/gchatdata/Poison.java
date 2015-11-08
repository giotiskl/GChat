package gchatdata;

import java.io.Serializable;

/**
 *      A class which serves to break out of the ObjectInputStream loop
 * of the server, for smooth shutdown.
 *
 *       The Poison class holds only a private boolean indicating whether the object in question
 * is to assist the shutdown procedure of the server.
 *
 *      Poison objects are used to assist smooth ObjectInputStream shutdowns, without
 * throwing an unnecessary EOFException, which can now be documented for its actual reason.

 *      A regular Poison is sent to the server when the Client clicks their disconnect button.
 * When a regular Poison object is to the server. The server sends it again to the client,
 * allowing them to disconnect, while the server successfully shuts down the ConnectionTask
 * for the Client in question.
 *
 *      A Poison object flagged as toShutdownServer is sent by the server to all clients, when
 * the server initiates its shutdown. Such an object is sent when the server owner clicks the
 * "Go Offline" button. This Poison is sent to all clients which then resend it to the server
 * allowing it to terminate all ConnectionTasks, before shutting down the ServerSocket.
 *
 */
public class Poison implements Serializable
{
    private static final long serialVersionUID = 7899912497983057704L;

    private final boolean toShutdownServer; //Flag used to assist the server shutdown procedure

    public Poison(boolean shutdownServer)
    {
        this.toShutdownServer = shutdownServer;
    }

    public boolean isToShutdownServer()
    {
        return toShutdownServer;
    }
}