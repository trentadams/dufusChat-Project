package dufusChat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class DufusChatClient extends Application {
    private TextArea taHistory = new TextArea();
    private TextArea taInput = new TextArea();
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private User user;

    @Override
    public void start(Stage primaryStage) {
        // Prompt user for their name before starting connection
        TextInputDialog dialog = new TextInputDialog("User");
        dialog.setTitle("Username");
        dialog.setHeaderText("Welcome to DufusChat");
        dialog.setContentText("Please enter your username:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get();
            int randomId = (int) (Math.random() * 1000);
            user = new User(name.trim(), randomId);
            setupUI(primaryStage);
            new Thread(() -> connectToServer()).start();
        } else {
            Platform.exit();
        }
        
    }

    private void setupUI(Stage primaryStage) {
        taHistory.setWrapText(true);
        taHistory.setEditable(false);
        taInput.setWrapText(true);

        BorderPane pane1 = new BorderPane(new ScrollPane(taHistory));
        pane1.setTop(new Label("Chat History"));
        BorderPane pane2 = new BorderPane(new ScrollPane(taInput));
        pane2.setTop(new Label("New Message"));

        VBox vBox = new VBox(5, pane1, pane2);

        Scene scene = new Scene(vBox, 400, 400);
        primaryStage.setTitle("DufusChat - " + user.getUsername());
        primaryStage.setScene(scene);
        primaryStage.show();

        taInput.setOnKeyPressed(event -> {
        	if (event.getCode() == KeyCode.ENTER) {
        	    event.consume(); // Prevent newline
        	    String msg = taInput.getText().trim();
        	    if (!msg.isEmpty()) {
        	        out.println(user.getUsername() + ": " + msg);
        	        taInput.clear();
        	    }
        	}
        });
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                appendText(message);
            }
        } catch (IOException e) {
            appendText("Connection error: " + e.getMessage());
        }
    }

    private void appendText(String text) {
        Platform.runLater(() -> taHistory.appendText(text + "\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
