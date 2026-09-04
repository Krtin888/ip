package chris.gui;

import chris.Chris;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Controls Chris's main chat window. */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Chris chris;

    /** Keeps the latest dialog visible as the conversation grows. */
    @FXML
    public void initialize() {
        dialogContainer.heightProperty().addListener(observable -> scrollPane.setVvalue(1.0));
    }

    /** Connects this window to Chris and displays the greeting. */
    public void setChris(Chris chris) {
        this.chris = chris;
        dialogContainer.getChildren().add(DialogBox.getChrisDialog(chris.startGui()));
    }

    /** Displays the entered command and Chris's response. */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.isBlank()) {
            return;
        }

        String response = chris.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input),
                DialogBox.getChrisDialog(response));
        userInput.clear();

        if (chris.isExitRequested()) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
            PauseTransition exitDelay = new PauseTransition(Duration.seconds(1));
            exitDelay.setOnFinished(event -> Platform.exit());
            exitDelay.play();
        }
    }
}
