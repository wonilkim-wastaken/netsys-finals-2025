import jdk.jshell.spi.ExecutionControl;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler {

    // Chat storage
    private List<Chat> chatList = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Socket> connectedUser = new ConcurrentHashMap<>();
    private Map<Socket, String> socketToUser = new ConcurrentHashMap<>();
    private Map<String, Chat> chatMap = new ConcurrentHashMap<>();

    private void ParseMessage(String message, Writer writer, String selfUser) throws IOException {

        if (!message.startsWith(":")) {
            Chat activeChat = chatMap.get(selfUser);
            if (activeChat == null){
                return;
            }
            String userToSend;
            if (activeChat.user1.equals(selfUser)){
                userToSend = activeChat.user2;
            }
            else{
                userToSend = activeChat.user1;
            }
            Socket  socket = connectedUser.get(userToSend);
            OutputStream out = socket.getOutputStream();
            BufferedWriter newWriter = new BufferedWriter(new OutputStreamWriter(out));
            newWriter.write(message);
            return;
        }

        String rest = message.substring(1);

        int space = rest.indexOf(' ');
        String cmd;
        String argsPart;

        if (space == -1) {
            cmd = rest;
            argsPart = "";
        } else {
            cmd = rest.substring(0, space);
            argsPart = rest.substring(space + 1).trim();
        }

        String[] args = argsPart.isEmpty()
                ? new String[0]
                : argsPart.split("\\s+");

        switch (cmd){
            case "login":
                if (args.length < 1){
                    // TODO : SEND ERR
                    writer.write("Missing username!");
                    break;
                }
                String newName = args[0];
                Socket clientSocket = connectedUser.remove(selfUser);
                connectedUser.put(newName, clientSocket);
                socketToUser.put(clientSocket, newName);
                break;
            case "connect":
                if (args.length < 1){
                    // TODO: SEND ERR
                    writer.write("Missing ID!");
                    break;
                }
                Chat chat = null;
                for (int i=0; i<chatList.size(); i++){
                    if (chatList.get(i).user1.equals(args[0]) && chatList.get(i).user2.equals(selfUser)){
                        chat = chatList.get(i);
                        chatMap.put(selfUser, chat);
                        // TODO : SEND CONNECTED CMD
                    }
                }
                if (chat == null){
                    // TODO : SEND CONNECTED CMD
                    chat = new Chat(selfUser, args[0]);
                    chatMap.put(selfUser, chat);
                }
                break;
            case "disconnect":
                if (chatMap.get(selfUser) == null){
                    // TODO: SEND ERROR
                    break;
                }
                Chat userChat = chatMap.get(selfUser);
                if (!chatMap.containsKey(userChat.GetOther(selfUser))){
                    chatList.remove(userChat);
                }
                else if (chatMap.get(userChat.GetOther(selfUser)).equals(userChat)){
                    chatList.remove(userChat);
                }
                chatMap.put(selfUser, null);
                // TODO: SEND DISCONNECTED CMD
                break;
            case "message": {
                Chat activeChat = chatMap.get(selfUser);
                if (activeChat == null) {
                    // TODO: SEND ERR
                    break;
                }
                String userToSend;
                if (activeChat.user1.equals(selfUser)) {
                    userToSend = activeChat.user2;
                } else {
                    userToSend = activeChat.user1;
                }
                Socket socket = connectedUser.get(userToSend);
                OutputStream out = socket.getOutputStream();
                BufferedWriter newWriter = new BufferedWriter(new OutputStreamWriter(out));

                //TODO: SEND MSG CMD
                newWriter.write(message);
                break;
            }
            case "file": {
                Chat activeChat = chatMap.get(selfUser);
                if (activeChat == null) {
                    //TODO: SEND ERR
                    break;
                }
                String userToSend;
                if (activeChat.user1.equals(selfUser)) {
                    userToSend = activeChat.user2;
                } else {
                    userToSend = activeChat.user1;
                }
                activeChat.FileContents = args[0];

                // TODO: SEND CONTENTS
                break;
            }
            case "download": {
                Chat activeChat = chatMap.get(selfUser);
                if (activeChat == null) {
                    //TODO: SEND ERR
                    break;
                }
                String userToSend;
                if (activeChat.user1.equals(selfUser)) {
                    userToSend = activeChat.user2;
                } else {
                    userToSend = activeChat.user1;
                }
                if (activeChat.FileContents == null || activeChat.FileContents.equals("")) {
                    //TODO: SEND ERR
                }
                else {
                    //TODO: SEND CMD
                }
                break;
            }
        }
    }

    public void main(String[] args) throws UnknownHostException {
        if (args.length != 1) {
            System.out.println("Usage: <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);



        try (ServerSocket socket = new ServerSocket(port)) {
            String hostname = InetAddress.getLocalHost().getHostName();
            System.out.println("run at " + hostname + " on " + port);
            while (true) {
                Socket clientSocket = socket.accept();
                Thread thread = new Thread(() -> {
                    System.out.println("Accepted connection from client at " +
                            clientSocket.getInetAddress());
                    try (InputStream in = clientSocket.getInputStream();
                         OutputStream out = clientSocket.getOutputStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
                        String line;
                        Random rand = new Random();
                        socketToUser.put(clientSocket, "user"+rand.nextInt());
                        //ASSIGN PLACEHOLDER
                        while ((line = reader.readLine()) != null) {
                            ParseMessage(line, writer, socketToUser.get(clientSocket));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


