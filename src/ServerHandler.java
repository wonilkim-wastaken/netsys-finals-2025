import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerHandler {
    private List<Chat> chatList = new ArrayList<>();
    private Map<Integer, Socket> connectedUser = new HashMap<>();

    private int currentAssigned = 0;

    private int AssignNewID(){
        return currentAssigned++;
    }

    private void CreateChat(int userID1, int userID2){
        Chat chat = new Chat(userID1, userID2);
        chatList.add(chat);
    }

    private void RemoveChat(int userID){
        chatList.remove(userID);
    }

    private Chat SearchByID(int userID){
        for (Chat chat : chatList){
            if (chat.userID1 == userID || chat.userID2 == userID){
                return chat;
            }
        }
        System.out.println("Chat with " + userID + " not found");
        return null;
    }

    private Socket GetSocketByID(int userID){
        // THIS DOES NOT WORK
        return connectedUser.get(new Integer(userID));
    }

    private void ReceiveMessage(String message, int senderID){
    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ExerciseA2 <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);


        try (ServerSocket socket = new ServerSocket(port)) {

        }
        catch (IOException e) {
            e.printStackTrace();
        }}
        /*
        String hostname = InetAddress.getLocalHost().getHostName();
        System.out.println("run at " + hostname + " on " + port);
        while (true) {
            Socket clientSocket = socket.accept();
            Thread thread = new Thread(()->{
                System.out.println("Accepted connection from client at " +
                        clientSocket.getInetAddress());
                try (InputStream in = clientSocket.getInputStream();
                     OutputStream out = clientSocket.getOutputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))){
                    String line;
                    int bytesRead = 0;
                    while ((line = reader.readLine()) != null) {
                        bytesRead += line.getBytes().length;
                        writer.write("Echo: " + line + "\n");
                    }
                    System.out.println("Client" +
                            clientSocket.getInetAddress() + ", echoed " +
                            + bytesRead + " bytes.");

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

*/
}


