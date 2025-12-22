import java.io.*;
import java.net.*;
import java.util.*;

public class ClientServerReplyHandler implements Runnable {
    private Socket client;
    private BufferedReader in;

    public ClientServerReplyHandler(Socket client) {
        this.client = client;
        try {
            this.in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> serverMsgParser(String msg) {
        HashMap<String, String> parsedMsg = new HashMap<>();
        String[] lv1Parsed = null;
        msg = msg.trim();
        lv1Parsed = msg.split(",");

        for (String s : lv1Parsed) {
            String[] KVsplited = s.split(":");
            parsedMsg.put(KVsplited[0], KVsplited[1]);
        }

        return parsedMsg;
    }

    private static void serverMsgDispatcher(HashMap<String, String> serverMsg) {
        String type = serverMsg.get("type");
        switch (type) {
            case "RESPONSE":
                handleResponse(serverMsg);
                break;
            case "NOTIFICATION":
                handleNotification(serverMsg);
                break;
            default:
                System.out.println("Unknown command");
        }
    }

    private static void handleResponse(HashMap<String, String> serverMsg) {
        int code = Integer.parseInt(serverMsg.get("CODE"));
        String message = serverMsg.get("MESSAGE");
        if (code != 200) {
            System.out.println(message);
        }
    }

    // CLIENT_JOINED: Client joined the chat.
    // CLIENT_LEFT: Client left the chat.
    // MESSAGE_SENT: Client received the message. / Server sent the message.

    private static String[] handleNotification(HashMap<String, String> serverMsg) {
        String event = serverMsg.get("EVENT");

    }

    @Override
    public void run() {
        // Display message ops.
        // reading loop should be implemented
        while (true) {
            try {
                String serverMsg = in.readLine();
                HashMap<String, String> parsedMsg = serverMsgParser(serverMsg);
                serverMsgDispatcher(parsedMsg);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
