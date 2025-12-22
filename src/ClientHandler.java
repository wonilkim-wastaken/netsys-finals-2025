import java.net.*;
import java.io.*;
import java.util.*;

public class ClientHandler {
    String username = null;
    Socket client = null;
    //BufferedReader in = null;
    PrintWriter out = null;

    private void setName(String username) {
        this.username = username;
    }

    private void sendConnectRequest(int userID) {
        // TODO - Need to make agreement about how to save connection info.
    }

    private void sendMessage(String message) {
        out.print(message);
        out.flush();
    }

    private void connectToServer(String hostName, int port) throws IOException {
        this.client = new Socket(hostName, port);
        //this.in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        this.out = new PrintWriter(this.client.getOutputStream(), true);
    }

    private void promptLogin() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = input.nextLine();

        String request = requestBuilder("login", "", "SEVER", username);
        sendMessage(request);

        // TODO - response logic needed to be added
        // notify success needed
        setName(username);
    }

    private static String requestBuilder(String command, String senderName, String SendeeName, String payload) {
        StringBuilder rb = new StringBuilder();
        rb.append("SENDER:").append(senderName).append(",");
        rb.append("SENDEE:").append(SendeeName).append(",");
        rb.append("METHOD:").append(command).append(",");
        rb.append("MESSAGE:").append(payload).append("\n");

        return rb.toString();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java ClientHandler <hostname> <port>");
            return;
        }

        ClientHandler handler = new ClientHandler();

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);

        try (BufferedReader consoleIn =  new BufferedReader(new InputStreamReader(System.in));) {
            handler.connectToServer(hostName, port);
            handler.promptLogin();

            // Message receiving ops.
            Thread receiver = new Thread(new ClientServerReplyHandler(this.client));
            receiver.start();

            while (true) {
                // Message sending ops.
                String message = consoleIn.readLine();
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
