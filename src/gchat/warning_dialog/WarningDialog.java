package gchat.warning_dialog;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * A Warning dialog to display messages in case of exception for JavaFX
 */
public class WarningDialog
{
    private Stage warningStage = new Stage();
    private String warning;

    public WarningDialog(String warning)
    {
        this.warning = warning;
    }

    public void display(Window owner)
    {
        VBox root = null;
        try
        {
            root = FXMLLoader.load(WarningDialogController.class.getResource("WarningDialogLayout.fxml"));
            warningStage.setScene(new Scene(root, 450, 200));
            warningStage.setTitle("Warning");
            warningStage.initModality(Modality.WINDOW_MODAL);
            warningStage.initOwner(owner.getScene().getWindow());
            warningStage.setResizable(false);
            warningStage.show();

            //really ugly way to get the inner TextFlow and add the warning message
            ((TextFlow)((VBox)((HBox)root.getChildren().get(0)).getChildren().get(1)).getChildren().get(0)).getChildren().add(new Text(warning));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}