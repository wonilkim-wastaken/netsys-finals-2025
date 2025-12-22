import java.io.*;
import java.net.*;

public class ClientServerReplyHandler implements Runnable {
    private Socket client;
    private BufferedReader in;

    public ClientServerReplyHandler(Socket client) {
        this.client = client;
        try {
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] serverMsgParser(String msg) {}

    private static String[] responseParser(String msg) {}

    private static String[] notificationParser(String msg) {}

    @Override
    public void run() {
        // Display message ops.
        // reading loop should be implemented

    }
}
