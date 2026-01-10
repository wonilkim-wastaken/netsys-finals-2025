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
    private List<User>  userList = Collections.synchronizedList(new ArrayList<>());
    private Map<User, Chat> chatMap = new ConcurrentHashMap<>();

    private void ParseMessage(String message, Writer writer, User selfUser) throws IOException {

        if (message == null || !message.startsWith(":")) {
            writer.write("Error: Invalid Message Format\n");
            writer.flush();
            return;
        }

        String[] parts = message.substring(1).split(" ", 2);
        String command = parts[0].toLowerCase();
        String payload = (parts.length > 1) ? parts[1] : "";
        String[] args = payload.isEmpty() ? new String[0] : payload.split(" ");

        switch (command) {
            case "message":
                if (payload.isEmpty()) {
                    writer.write("ERR: Command ':message' requires a message.\n");
                    writer.flush();
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :message arguments");
                    break;
                }

                if (chatMap.get(selfUser) == null) {
                    writer.write("ERR: Not connected to a chat.\n");
                    System.out.println("[WARN] " + selfUser.Username + " sent :message without connecting to the chat");
                    writer.flush();
                    break;
                }

                chatMap.get(selfUser).AddMessage(selfUser, payload);
                chatMap.get(selfUser).MessageAllUsers(selfUser.Username+ ": " + payload);
                System.out.println("[INFO] " + selfUser.Username + " sent msg " + payload);
                writer.write("OK\n");
                writer.flush();
                break;

            case "join":
                if (args.length != 1) {
                    writer.write("ERR: Command ':" + command + "' requires 1 argument.\n");
                    writer.flush();
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :join arguments");
                    break;
                }
                Chat chat = null;
                for (int i = 0; i < chatList.size(); i++) {
                    if (chatList.get(i).Chat_ID.equals(args[0])) {
                        chat = chatList.get(i);
                    }
                }
                if (chat == null) {
                    writer.write("ERR: Server not found\n");
                    System.out.println("[INFO] " + selfUser.Username + " tried to join chat " + args[0]);
                    System.out.println("[WARN] Chat " + args[0] + "does not exist!");
                    writer.flush();
                    break;
                }
                if (chatMap.get(selfUser) != null) {
                    chatMap.get(selfUser).DisconnectUser(selfUser);
                }
                chat.AddUser(selfUser);
                chatMap.put(selfUser, chat);
                writer.write("OK\n");
                System.out.println("[INFO] " + selfUser.Username + " joined chat " + args[0]);
                writer.flush();
                for (int i = 0; i < chat.Messages.size(); i++) {
                    writer.write(chat.Messages.get(i).toString() + "\n");
                }
                writer.flush();
                break;

            case "create":
                if (args.length != 1) {
                    writer.write("ERR: Command ':" + command + "' requires 1 argument.\n");
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :create argument");
                    break;
                }
                Chat newChat = new Chat(args[0]);
                chatList.add(newChat);
                writer.write("OK\n");
                writer.flush();
                System.out.println("[INFO] " + selfUser.Username + " created chat " + args[0]);
                break;

            case "username":
                if (args.length != 1) {
                    writer.write("ERR: Command ':" + command + "' requires 1 argument.\n");
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :create argument");
                    break;
                }
                User otherUser = null;
                for (int i = 0; i < userList.size(); i++) {
                    if (userList.get(i).Username.equals(args[0])) {
                        otherUser = userList.get(i);
                    }
                }
                if (otherUser != null) {
                    writer.write("ERR: Username already exists\n");
                    System.out.println("[INFO] " + selfUser.Username + " set name as " + args[0]);
                    System.out.println("[WARN] " + args[0] + " already exists!");
                    writer.flush();
                    break;
                }
                selfUser.Username = args[0];
                writer.write("OK\n");
                writer.flush();
                break;

            case "leave":
                if (args.length != 0) {
                    writer.write("ERR: Command ':" + command + "' takes no arguments.\n");
                    writer.flush();
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :leave argument");
                    break;
                }
                if (chatMap.get(selfUser) == null) {
                    writer.write("ERR: Not connected to any server.\n");
                    writer.flush();
                    System.out.println("[WARN} " + selfUser.Username + " tried to leave chat but not connected to any server.");
                    break;
                }
                chatMap.get(selfUser).DisconnectUser(selfUser);
                chatMap.remove(selfUser);
                writer.write("OK\n");
                writer.flush();
                System.out.println("[INFO] " + selfUser.Username + " left chat");
                break;

            case "whisper": {
                String[] whisperArgs = payload.split(" ", 2);
                if (whisperArgs.length != 2) {
                    writer.write("ERR: Command ':whisper' requires a username and a message.\n");
                    writer.flush();
                    System.out.println("[WARN] " +  selfUser.Username + " sent invalid :whisper argument");
                    break;
                }

                String targetName = whisperArgs[0];
                String messageText = whisperArgs[1];

                otherUser = null;
                for (User u : userList) {
                    if (u.Username.equals(targetName)) {
                        otherUser = u;
                        break;
                    }
                }

                if (otherUser == null) {
                    writer.write("ERR: Username not found.\n");
                    writer.flush();
                    System.out.println("[INFO] " +  selfUser.Username + " tried to whisper " + targetName + "but not found");
                    break;
                }

                otherUser.UserBufferedWriter.write(selfUser.Username + " whispered to you: " + messageText + "\n");
                otherUser.UserBufferedWriter.flush();

                writer.write("OK\n");
                writer.flush();
                System.out.println("[INFO] " +  selfUser.Username + " tried to whisper " + targetName);
                break;
            }


            case "file":
                int firstSpace = payload.indexOf(' ');
                if (firstSpace == -1) {
                    writer.write("ERR: Command ':file' requires filename and content\n");
                    writer.flush();
                    System.out.println("[WARN] " +  selfUser.Username + " sent invalid :file argument");
                    break;
                }
                String fileName = payload.substring(0, firstSpace);
                String content = payload.substring(firstSpace + 1);
                if (chatMap.get(selfUser) == null) {
                    writer.write("ERR: Not connected to any server.\n");
                    writer.flush();
                    System.out.println("[INFO] " +  selfUser.Username + " tried to send file "
                            + fileName + ", but not connected to any server.");
                    break;
                }
                var currentChat = chatMap.get(selfUser);
                if (currentChat == null) {
                    writer.write("ERR: Not in a chat room.\n");
                    writer.flush();
                    System.out.println("[INFO] " +  selfUser.Username + " tried to send file "
                            + fileName + ", but not in a chat room.");
                    break;
                }

                String chatDirectory = currentChat.Chat_ID + "/files";

                try {
                    java.nio.file.Path dirPath = java.nio.file.Paths.get(chatDirectory);
                    java.nio.file.Path filePath = dirPath.resolve(fileName);
                    java.nio.file.Files.createDirectories(dirPath);
                    java.nio.file.Files.writeString(filePath, content);
                    currentChat.AddFile(selfUser, fileName);
                    currentChat.MessageAllUsers("MSG: " + fileName);
                    writer.write("OK\n");
                    System.out.println("[INFO] " +  selfUser.Username + " uploaded file " + fileName);
                } catch (IOException e) {
                    writer.write("ERR: Could not save file to " + chatDirectory + "\n");
                    System.out.println("[FAIL] " + selfUser.Username + " uploaded file " + fileName
                            + " but could not save file to " + chatDirectory + "\n");
                }
                writer.flush();
                break;

            case "download":
                if (args.length != 1) {
                    writer.write("ERR: Command ':download' requires 1 argument (filename).\n");
                    writer.flush();
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :download argument");
                    break;
                }
                String requestedFile = args[0];
                chat = chatMap.get(selfUser);

                if (chat == null) {
                    writer.write("ERR: Not in a chat room.\n");
                    writer.flush();
                    System.out.println("[WARN] " +  selfUser.Username + " tried to download file but not in a chat room");
                    break;
                }
                String filePathString = chat.Files.get(requestedFile);

                if (filePathString == null) {
                    writer.write("ERR: File not found in this chat.\n");
                } else {
                    try {
                        java.nio.file.Path path = java.nio.file.Paths.get(filePathString);
                        if (java.nio.file.Files.exists(path)) {
                            String fileContent = java.nio.file.Files.readString(path);
                            writer.write("FILE: " + "\"" + fileContent + "\"" + "\n");
                            System.out.println("[INFO] " +  selfUser.Username + " downloaded file " + requestedFile);
                        } else {
                            writer.write("ERR: File reference exists but file is missing from disk.\n");
                            System.out.println("[FAIL] " + selfUser.Username + " tried to download file " + requestedFile);
                            System.out.println("[FAIL] Reference for " + requestedFile + " exists, but file is missing from disk");
                        }
                    } catch (IOException e) {
                        writer.write("ERR: Could not read file content.\n");
                        System.out.println("[FAIL] Could not read file content from file " + requestedFile);
                    }
                }
                writer.flush();
                break;

            case "list":
                if (args.length != 0) {
                    writer.write("ERR: Command ':" + command + "' requires 1 argument.\n");
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :list argument");
                    break;
                }
                for (int i = 0; i < chatList.size(); i++) {
                    writer.write(chatList.get(i).toString() + "\n");
                }
                writer.flush();
                System.out.println("[WARN] " +  selfUser.Username + " requested list of user associated to the caht");
                break;

            case "quit":
                if (args.length != 0) {
                    writer.write("ERR: Command ':" + command + "' requires 1 argument.\n");
                    System.out.println("[WARN] " + selfUser.Username + " sent invalid :quit argument");
                    break;
                }
                writer.write("OK\n");
                writer.flush();
                System.out.println("[INFO] " +  selfUser.Username + " quitted");
                break;

            default:
                writer.write("ERR: Unknown command '" + command + "'\n");
                System.out.println("[WARN] " + selfUser.Username + " sent unknown command " + command);
                break;
        }
        writer.flush();
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
                        User user = new User();
                        user.Username = "user"+rand.nextInt(10000);
                        user.UserSocket = clientSocket;
                        user.UserBufferedWriter = writer;
                        userList.add(user);
                        while ((line = reader.readLine()) != null) {
                            ParseMessage(line, writer, user);
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


