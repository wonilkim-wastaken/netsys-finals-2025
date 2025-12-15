import java.util.ArrayList;
import java.util.List;

public class Chat {

    private int userID1;
    private int userID2;
    private List<String> messageHistory;

    public Chat(int userID1, int userID2) {
        this.userID1 = userID1;
        this.userID2 = userID2;
        this.messageHistory = new ArrayList<String>();
    }

    public void addMessage(String message) {
        messageHistory.add(message);
    }

    public void sendMessage(int senderID, String message) {
        if (senderID != userID1 && senderID != userID2) {
            throw new IllegalArgumentException("Invalid sender");
        }

        String record = "[" + senderID + "] " + message;
        messageHistory.add(record);
    }

    public void sendFile(int senderID, String filePath) {
        if (senderID != userID1 && senderID != userID2) {
            throw new IllegalArgumentException("Invalid sender");
        }

        String record = "[" + senderID + "] FILE: " + filePath;
        messageHistory.add(record);
    }

    public void deleteMessage(int index) {
        if (index < 0 || index >= messageHistory.size()) {
            throw new IllegalArgumentException("Message index out of range");
        }

        messageHistory.remove(index);
    }

    public List<String> getMessageHistory() {
        return new ArrayList<String>(messageHistory);
    }

    public int getOpponent(int senderID) {
        if (senderID == userID1) {
            return userID2;
        }

        if (senderID == userID2) {
            return userID1;
        }

        throw new IllegalArgumentException("User not found");
    }
}
