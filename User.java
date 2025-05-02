package dufusChat;

public class User {
    private String username;
    private int clientId;

    public User(String username, int clientId) {
        this.username = username;
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public int getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return username;
    }
}
