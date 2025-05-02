package dufusChat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class DufusChatServer extends Application {
    private TextArea taServer = new TextArea();
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private int clientIdCounter = 1;

    @Override
    public void start(Stage primaryStage) {
        BorderPane pane = new BorderPane(new ScrollPane(taServer));
        Scene scene = new Scene(pane, 400, 400);
        primaryStage.setTitle("DufusChat Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(this::startServer).start();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(8000);
            appendText("Server started on port 8000...");

            while (true) {
                Socket socket = serverSocket.accept();
                int clientId = clientIdCounter++;
                ClientHandler handler = new ClientHandler(socket, clientId);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            appendText("Server error: " + e.getMessage());
        }
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
        appendText(message);
    }

    private void appendText(String text) {
        Platform.runLater(() -> taServer.appendText(text + "\n"));
    }

    class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private User user;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.user = new User("User" + clientId, clientId);
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                broadcast(user.getUsername() + " joined the chat");

                String msg;
                while ((msg = in.readLine()) != null) {
                    broadcast(user.getUsername() + ": " + msg);
                }
            } catch (IOException e) {
                appendText("Connection error with " + user.getUsername());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                clients.remove(this);
                broadcast(user.getUsername() + " left the chat");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
