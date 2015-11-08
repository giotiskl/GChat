package com.giotis_kal.gchatclient;

import gchatdata.ChatMessage;
import gchatdata.Poison;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chat Client
 */
public class GChatClient
{
    //username field cached from GUI
    private String username;

    //References to socket and ObjectOutputStream
    private Socket socket;
    private ObjectOutputStream writer;

    //Fields used to initiate socket connection
    private String host; //The host and port
    private int port;    //to connect to.

    //Internal ref to app for updates
    private ClientController app;

    /**
     * Public constructor of gchatclient
     * @param host host to connect to (points to the server)
     * @param port port to connect to at host address
     * @param app internal reference to the application's GUI Window
     */
    public GChatClient(String host, int port, ClientController app)
    {
        this.host = host;
        this.port = port;
        this.app = app;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Connects to the Chat Server. Once connected the ReaderTask thread is
     * initialized which handles all incoming server input.
     *
     * @throws IOException in case connection is impossible because of the following two exceptions
     * @throws ConnectException if access is denied by host
     * @throws UnknownHostException if an attempt is made to connect to an unknown howst
     */
    public void connect() throws IOException
    {
        socket = new Socket(host, port);
        writer = new ObjectOutputStream(socket.getOutputStream());

        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(new SocketTask(socket)); //Handle all input from server
        exec.shutdown(); //Make sure it shuts down once work done

        app.appendMessage("Connected to server!");
    }

    /**
     * Used to initialize client shutdown in case the user presses the disconnect button
     * or closes the application's window.
     *
     * @throws IOException in case closing the socket's I/O fails
     */
    public void shutdownSocketIO()
    {
        try
        {
            sendMessage(new Poison(false)); //Send Poison to let server know of our intention to disconnect
            socket.shutdownOutput();        //Shut down output
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server, which is broadcast to all clients
     * currently connected.
     *
     * @param msg the message sent to the server which is broadcast to all clients
     */
    public void sendMessage(Object msg)
    {
        try
        {
            writer.writeObject(msg);
            writer.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether the client is connected to the server or not
     *
     * @return connection status
     */
    public boolean isConnected()
    {
        return socket!= null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Private inner class used as the Runnable which handles
     * server input.
     */
    private class SocketTask implements Runnable
    {
        private Socket socket; //internal reference to socket

        public SocketTask(Socket socket)
        {
            this.socket = socket;
        }

        /**
         * Reader task used to display messages to clients.
         *
         * Disconnections are assisted by using Poison objects.
         * @see Poison
         */
        @Override
        public void run()
        {
            //Send empty message to server for it to cache username
            sendMessage(new ChatMessage(getUsername(), ""));

            //Handle input from server
            try (ObjectInputStream reader = new ObjectInputStream(socket.getInputStream()))
            {
                handleServerInput(reader);
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            } finally
            {
                closeSocketAndResetFrame();
            }
        }

        /**
         * Handles input from server in a loop using an ObjectInputStream. To break out of the loop correctly
         * a Poison object is sent in case the user presses disconnect or the server shuts down.
         *
         * If the Poison object is flagged "toShutDownServer" then it is resent to the server to allow
         * the corresponding ConnectionTask of the server to terminate.
         *
         * @param reader the ObjectInputStream that handles input from the server
         * @throws ClassNotFoundException in case an Object of unknown class is received
         * @throws IOException if the stream fails
         * @throws EOFException if the stream reaches EOF
         * @throws SocketException if the socket closes abruptly
         *
         * @see Poison
         */
        private void handleServerInput(ObjectInputStream reader) throws ClassNotFoundException, IOException
        {
            Object msg;
            while(true)
            {
                msg = reader.readObject();

                if (msg instanceof Poison) //in case of Poison
                {
                    //if Poison is flagged toShutdownServer then resend it to server
                    if (((Poison) msg).isToShutdownServer()) sendMessage(msg);
                    break;
                }
                else if (msg instanceof ChatMessage) //handle the message
                {
                    app.appendMessage(msg.toString());
                }
                else if (msg instanceof ArrayList<?>)
                {
                    app.updateUsernameList((ArrayList<String>)msg);
                }
            }
        }

        private void closeSocketAndResetFrame()
        {
            try
            {
                socket.close();
                app.appendMessage("Disconnected from server...");
                app.toggleConnected();
            } catch (IOException e) //In case of socket close failure (i.e. if socket is already closed)
            {
                e.printStackTrace();
            }
        }
    }
}
