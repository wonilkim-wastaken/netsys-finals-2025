import java.util.ArrayList;
import java.util.List;

public class Chat {

    private final String userID1;
    private final String userID2;
    private final List<String> messageHistory;
    private String fileContents;

    public Chat(String userID1, String userID2) {
        this.userID1 = userID1;
        this.userID2 = userID2;
        this.messageHistory = new ArrayList<String>();
    }

    public void addMessage(String message) {
        messageHistory.add(message);
    }

    public void sendMessage(String senderID, String message) {
        if (senderID != userID1 && senderID != userID2) {
            throw new IllegalArgumentException("Invalid sender");
        }

        String record = "[" + senderID + "] " + message;
        messageHistory.add(record);
    }

    public void sendFile(String senderID, String filePath) {
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

    public String getOpponent(String senderID) {
        if (senderID == userID1) {
            return userID2;
        }

        if (senderID == userID2) {
            return userID1;
        }

        throw new IllegalArgumentException("User not found");
    }

    public void setFile(String fileContents) {
        this.fileContents = fileContents;
    }

    public String getFile() {
        return fileContents;
    }

    public void clearFile() {
        this.fileContents = null;
    }
}

