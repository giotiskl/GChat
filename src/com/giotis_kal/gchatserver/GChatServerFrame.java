package com.giotis_kal.gchatserver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

/**
 * The Frame which holds the TCP Chat Server functionality
 */
public class GChatServerFrame extends JFrame
{
    private static final long serialVersionUID = 2809486332370935187L;

    private JLabel portLabel;
    private JTextField portField;
    private JButton goOnlineButton;
    private JTextArea msgArea;
    private JPanel northwest, north;

    private JList<String> usersList;
    private DefaultListModel<String> listModel;

    private GChatServer chatServer; //the server manager

    public GChatServerFrame() throws HeadlessException
    {
        super("GChat Server");

        north = new JPanel(new BorderLayout());
        northwest = new JPanel(new FlowLayout());

        portLabel = new JLabel("Port: ");
        portField = new JTextField("54321", 4);
        /**
         * Go Online button has two states:
         * 1) Go Online
         * 2) Go Offline
         * depending on whether the server is currently on or off
         */
        goOnlineButton = new JButton("Go Online");
        goOnlineButton.addActionListener(actionEvent -> {
            if (goOnlineButton.getText().contains("Online")) //Check button state
            {
                //do some error checking
                boolean validInput = portField.getText().length() >= 4 && containsOnlyDigits(portField.getText());
                if (!validInput)
                {
                    displayPopUp("Invalid port number!", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //if ok - put server online
                chatServer = new GChatServer(Integer.parseInt(portField.getText()), GChatServerFrame.this);

                ExecutorService exec = Executors.newSingleThreadExecutor();
                exec.submit(() -> {
                    toggleConnectivityGUI();
                    try
                    {
                        chatServer.putOnline();
                    } catch (IOException e)
                    {
                        displayPopUp("Socket already in use!", "Error", JOptionPane.WARNING_MESSAGE);
                        toggleConnectivityGUI();
                    }
                });
                exec.shutdown(); //terminate executor
            }
            else
            {
                chatServer.shutdown();
                toggleConnectivityGUI();
            }
        });

        northwest.add(portLabel);
        northwest.add(portField);

        north.add(northwest, BorderLayout.WEST);
        north.add(goOnlineButton, BorderLayout.EAST);

        add(north, BorderLayout.NORTH);

        msgArea = new JTextArea();
        msgArea.setEditable(false);
        msgArea.setBackground(Color.white);
        msgArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(msgArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        usersList = new JList<>(listModel);
        usersList.setFixedCellWidth(100);

        add(usersList, BorderLayout.EAST);

        //add shutdown logic to window
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (chatServer!= null) chatServer.shutdown();
                super.windowClosing(e);
            }
        });
    }

    //Methods to add/remove a username to model list of JList
    public void addUsername(String username)
    {
        listModel.addElement(username);
    }
    public void removeUsername(String username)
    {
        listModel.removeElement(username);
    }
    public ArrayList<String> getUsernameList() {
        return Collections.list(listModel.elements());
    }

    //Used to display pop-up messages on the ETD
    public void displayPopUp(String msg, String title, int msgType)
    {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, msg, title, msgType));
    }

    //Used to update the statuses in the JTextArea of the frame
    public void printMsg(String msg)
    {
        SwingUtilities.invokeLater(() -> msgArea.append(">>>" + msg + "\r\n"));
    }

    /**
     * Quick function hacked together to check whether a string consists solely of numbers or not
     * @param s the string
     * @return whether it contains only digits
     */
    private boolean containsOnlyDigits(String s)
    {
        for (int i = 0; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i))) return false;

        return true;
    }

    /**
     * Toggles the JTextField and button of the frame
     */
    private void toggleConnectivityGUI()
    {
        SwingUtilities.invokeLater(() -> {
            portField.setEnabled(!portField.isEnabled());
            goOnlineButton.setText(goOnlineButton.getText().contains("Online") ? "Go Offline" : "Go Online");
        });
    }
}