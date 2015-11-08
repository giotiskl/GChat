package com.giotis_kal.gchatclient;

import gchat.warning_dialog.WarningDialog;
import gchatdata.ChatMessage;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientController implements Initializable
{
    @FXML
    private Button connectButton;
    @FXML
    private TextField addressField, portField, usernameField, chatBox;
    @FXML
    private TextArea msgArea;
    @FXML
    private ListView<String> usersListView;
    private ObservableList<String> usersList = FXCollections.observableArrayList();

    private GChatClient client;
    private BooleanProperty connected = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        //Added a ChangeListener to connected BooleanProperty which toggles the interface
        //when the user is connected to the server
        connected.addListener((changeListener) -> {
            Platform.runLater(() -> {
                addressField.setDisable(connected.get());
                portField.setDisable(connected.get());
                usernameField.setDisable(connected.get());
                connectButton.setText(connected.get() ? "Disconnect" : "Connect");
                clearUsernameViewList();
            });
        });

        usersListView.setItems(usersList);
    }

    /**
     * Appends new message to the client's message area.
     * @param msg new message to be appended
     */
    public void appendMessage(String msg)
    {
        Platform.runLater(() -> msgArea.appendText(msg + "\r\n"));
    }

    /**
     * Toggles the UI between connected/disconnected state in conjunction with the BooleanProperty's listener
     */
    public void toggleConnected()
    {
        this.connected.set(!connected.get());
    }

    public void updateUsernameList(ArrayList<String> newNameList)
    {
        Platform.runLater(() -> {
            usersList = FXCollections.observableArrayList(newNameList);
            usersListView.setItems(usersList);
        });
    }

    private void clearUsernameViewList()
    {
        Platform.runLater(() -> usersList.clear());
    }

    public void tryConnect()
    {
        if (connectButton.getText().equals("Connect"))
        {
            //do some error checking before attempting connection
            boolean validInput = portField.getText().length() >= 4 && containsOnlyDigits(portField.getText())
                    && Integer.parseInt(portField.getText()) < 65535;
            if (!validInput)
            {
                displayWarningDialog("Invalid port number!");
                return;
            }
            if (usernameField.getText().length() == 0)
            {
                displayWarningDialog("You must enter a username!");
                return;
            }

            //and set up the client
            client = new GChatClient(addressField.getText(), Integer.parseInt(portField.getText()), this);

            try
            {
                client.connect();
                client.setUsername(usernameField.getText());
                toggleConnected();
            }
            catch (UnknownHostException e)
            {
                displayWarningDialog("Host could not be reached!");
            }
            catch (ConnectException e)
            {
                displayWarningDialog("Connection refused!");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            client.shutdownSocketIO(); //invoke this to make client disconnect
        }
    }

    @FXML
    private void sendMessage()
    {
        boolean canProceed = chatBox.getText().length() > 0 && client != null && client.isConnected(); //error checking

        if (canProceed)
        {
            client.sendMessage(new ChatMessage(client.getUsername(), chatBox.getText()));
            Platform.runLater(() -> chatBox.setText(""));
        }
    }

    @FXML
    private void exitApp()
    {
        Stage window = (Stage)connectButton.getScene().getWindow();
        window.getOnCloseRequest().handle(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        window.hide();
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
     * Instantiates a new WarningDialog modal dialog.
     * @param warning message to be displayed.
     */
    private void displayWarningDialog(String warning)
    {
        new WarningDialog(warning).display(connectButton.getScene().getWindow());
    }

    /**
     * Called in main to set up window listeners
     */
    public void setupWindowListeners()
    {
        connectButton.getScene().getWindow()
                .setOnCloseRequest(windowEvent -> {
                    if (client != null && client.isConnected()) client.shutdownSocketIO();
                });
    }
}