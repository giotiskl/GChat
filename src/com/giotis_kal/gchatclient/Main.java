package com.giotis_kal.gchatclient;

import com.giotis_kal.gchatserver.GChatServerFrame;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

 /**
 *      GChat is an application I developed purely for learning purposes while trying
 * to get a grip on networking. It comes with a (hopefully) functional multi-threaded server
 * and a client.
  *
 *      The client application is developed in JavaFX to offer a better-looking
 * UI and the basic server application is developed in Swing. Both are packed here,
 * but they should be compiled and run separately to test the final product with multiple clients.
  *
 *      The client and the server communicate with each other using object streams over TCP/IP.
 * Thus, the package gchatdata is common and must be compiled with both the server and the
 * client for correct communication. They communicate by sending each other ChatMessage objects.
 * To shutdown their communication, they dispatch to each other Poison objects.
 *
 * @author Giotis Kaltsikis
 */

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientLayout.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("G Chat");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(550);
        primaryStage.setMinHeight(400);
        primaryStage.show();

        //WindowListener that closes down the socket's I/O on app exit
        ((ClientController)fxmlLoader.getController()).setupWindowListeners();
    }


    public static void main(String[] args) {
        //SwingUtilities.invokeLater(Main::displayGChatServerFrame);
        launch(args);
    }

    private static void displayGChatServerFrame()
    {
        GChatServerFrame serverApp = new GChatServerFrame();
        serverApp.setSize(640, 480);
        serverApp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        serverApp.setLocationByPlatform(true);
        serverApp.setVisible(true);
    }
}
