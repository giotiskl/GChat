package com.giotis_kal.gchatserver;

import gchatdata.ChatMessage;
import gchatdata.Poison;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The implementation of a multithreaded TCP Chat Server, able to handle
 * multiple clients, in a virtual public chat room.
 */
public class GChatServer
{
    //Handles multithreading
    private ExecutorService exec;

    //Networking variables
    private ServerSocket serverSocket;
    private int portNumber;

    //maintain reference to JFrame
    private GChatServerFrame frame;

    //internal list of clients
    private List<ObjectOutputStream> clients;
    private final Object clientsLock = new Object();

    /**
     * Public constructor of GChatServer
     *
     * @param portNumber the port number to set up the server at
     * @param frame an internal reference to the JFrame managing the server (used for status updates)
     *              @See GChatServerFrame's printMsg() method.
     */
    public GChatServer(int portNumber, GChatServerFrame frame)
    {
        this.exec = Executors.newCachedThreadPool();
        this.portNumber = portNumber;
        this.frame = frame;

        this.clients = new ArrayList<>(); //implemented as an arraylist
    }

    /**
     * Sets up the TCPServer and puts it in waiting state for connections.
     * Whenever a connection occurs, a ConnectionHandler task is spawned in the cached thread pool.
     *
     * @throws IOException in case the socket chosen is already in use by another app on the localhost
     */
    public void putOnline() throws IOException
    {
        frame.printMsg("Attempting connection...");
        serverSocket = new ServerSocket(portNumber); //Set up the server
        frame.printMsg("Connected successfully to port " + portNumber);
        frame.printMsg("Server up and running...");

        while (true) //Infinite loop enter, waiting for client connections.
        {
            try
            {
                frame.printMsg("Waiting for client connection...");
                Socket socket = serverSocket.accept();

                exec.submit(new ConnectionHandler(socket)); //Start thread to handle connection
            }
            catch (IOException e)
            {
                frame.printMsg("Server shut down (Reason: " + e.getMessage() + ")");
                break;
            }
        }
    }

    /**
     * Closes the server socket, shuts down the executor and clears the clients list
     */
    public void shutdown()
    {
        try
        {
            if (serverSocket != null)
            {
                shutdownClients();
                serverSocket.close();
                exec.shutdown();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Used to broadcast a client's message to all connected clients.
     *
     * @param msg the message to be broadcast
     * @throws IOException in case writing or flushing fails, in which case
     *         the handling method prints the stacktrace
     */
    private void broadcastMessage(Object msg) throws IOException
    {
        synchronized (clientsLock) //avoid iterating while another client might be added
        {
            for (ObjectOutputStream writer : clients)
            {
                writer.writeObject(msg);
                writer.flush();
            }
        }
    }

    /**
     * Shuts down all clients to allow for server shutdown by dipatching
     * Poison objects
     *
     * @throws IOException if writeObject method fails.
     * @see Poison
     */
    private void shutdownClients() throws IOException
    {
        synchronized (clientsLock)
        {
            for (ObjectOutputStream writer : clients) writer.writeObject(new Poison(true));
        }
        clients.clear();
    }

    /**
     * Private inner class responsible for handling new incoming
     * connections with clients, getting their I/O streams.
     */
    private class ConnectionHandler implements Runnable
    {
        private Socket socket; //endpoint between server-client

        public ConnectionHandler(Socket socket)
        {
            this.socket = socket;
        }

        /**
         * Handles the connection between server and client. Whenever a client sends a message,
         * the server broadcasts it to all clients including the sender.
         *
         * The infinite reading loop is broken when a Poison object is received.
         *
         * @see Poison
         */
        @Override
        public void run()
        {
            ObjectOutputStream writer = null; //initialize output stream
            String username = null; //reference that caches the name of the user connected

            try (ObjectInputStream reader = new ObjectInputStream(socket.getInputStream()))
            {
                //Get ObjectOutputStream of this specific client and cache it
                writer = new ObjectOutputStream(socket.getOutputStream());

                synchronized (clientsLock)
                {
                    clients.add(writer); //keep track for message forwarding
                }

                //Cache username and update username list
                username = ((ChatMessage) reader.readObject()).getUsername();
                frame.addUsername(username);
                frame.printMsg(username == null ? "User" : username + " connected (IP: " + socket.getRemoteSocketAddress() + ")"); //Log info
                broadcastMessage(frame.getUsernameList()); //update other clients jList with new username

                Object msg;
                while (true)
                {
                    msg = reader.readObject(); //receive msg

                    if (msg instanceof Poison) //in case of Poison
                    {
                        //In case of regular Poison shutdown, resend it to the client
                        //to let them disconnect. Then shut down ConnectionHandler.
                        if (!((Poison) msg).isToShutdownServer()) writer.writeObject(msg);
                        break;
                    }
                    if (msg instanceof ChatMessage) //in case of regular Message
                    {
                        //cache username
                        username = ((ChatMessage) msg).getUsername();
                        //then print and broadcast msg
                        frame.printMsg(msg.toString());
                        broadcastMessage(msg); //broadcast msg to all connected clients
                    }
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (writer != null)
                {
                    synchronized (clientsLock)
                    {
                        clients.remove(writer); //remove writer from client list if it's cached
                    }
                    try
                    {
                        frame.removeUsername(username); //remove username from JList
                        broadcastMessage(frame.getUsernameList()); //update other clients jList with new list
                        writer.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                frame.printMsg(username == null ? "User" : username + " " + socket.getRemoteSocketAddress() + " disconnected from server"); //log d/c
            }
        }
    }
}