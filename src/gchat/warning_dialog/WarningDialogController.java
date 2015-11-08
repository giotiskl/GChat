package gchat.warning_dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class WarningDialogController
{
    @FXML
    private TextArea warningArea;
    @FXML
    private Button okButton;

    public void closeDialog()
    {
        okButton.getScene().getWindow().hide();
    }
}
