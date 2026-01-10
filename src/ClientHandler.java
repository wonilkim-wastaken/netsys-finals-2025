import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientHandler {

    private static BufferedWriter serverWriter;
    private static volatile String lastCommandSent;
    private static volatile String currentRoom = null;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (
                Socket socket = new Socket(host, port);
                BufferedReader serverReader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())
                );
                BufferedReader userInput = new BufferedReader(
                        new InputStreamReader(System.in)
                )
        ) {
            serverWriter = writer;
            System.out.println("Connected to server.");

            clearOutput();
            printWelcome();
            printPrefix();

            Thread listener = new Thread(() -> listenToServer(serverReader));
            listener.setDaemon(true);
            listener.start();

            String input;
            while ((input = userInput.readLine()) != null) {

                String normalized = normalizeInput(input);
                if (normalized == null) {
                    continue;
                }

                send(normalized);
                //eraseLastLine();

                if (normalized.equalsIgnoreCase(":quit")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void eraseLastLine() {
        System.out.print("\033[1A"); // move cursor up
        System.out.print("\033[2K"); // clear entire line
    }

    private static void listenToServer(BufferedReader serverReader) {
        try {
            String line;
            while ((line = serverReader.readLine()) != null) {
                System.out.print("\033[2K");

                if (line.equals("OK")) {
                    handleOk();
                    printPrefix();
                    continue;
                }

                if (line.startsWith("ERR:")) {
                    System.out.println(line);
                    printPrefix();
                    continue;
                }

                if (line.startsWith("FILE: ")) {
                    handleFileDownload(line);
                    printPrefix();
                    continue;
                }
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }

    private static void handleOk() {
        if (lastCommandSent == null) return;

        if (lastCommandSent.startsWith(":join ")) {
            String server = lastCommandSent.substring(6);
            System.out.println("You are now in server " + server);
            currentRoom = server;
        }
        else if (lastCommandSent.startsWith(":create ")) {
            String server = lastCommandSent.substring(8);
            System.out.println("Server " + server + " created");
        }
        else if (lastCommandSent.startsWith(":leave")) {
            System.out.println("You left the server");
            currentRoom = null;
        }
        else if  (lastCommandSent.startsWith(":username")) {
            String username = lastCommandSent.substring(9);
            System.out.println("You set your username to " + username);
        }
    }

    private static String normalizeInput(String input) {
        input = input.trim();
        if (input.isEmpty()) return null;

        if (input.equalsIgnoreCase(":help")) {
            printWelcome();
            return null;
        }

        if (input.equalsIgnoreCase(":clear")) {
            clearOutput();
            printPrefix();
            return null;
        }

        if (!input.startsWith(":")) {
            return ":message " + input;
        }

        if (input.startsWith(":file ")) {
            return prepareFileCommand(input);
        }

        return input;
    }

    private static String prepareFileCommand(String input) {
        String[] parts = input.split(" ", 2);
        if (parts.length != 2) {
            System.out.println("Usage: :file <filename>");
            return null;
        }

        String filename = parts[1];

        try {
            String content = Files.readString(Path.of(filename))
                    .replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\"", "\\\"");

            return ":file " + filename + " \"" + content + "\"";

        } catch (IOException e) {
            System.out.println("Could not read file: " + filename);
            return null;
        }
    }

    private static void handleFileDownload(String line) {
        try {
            int firstQuote = line.indexOf('"');
            int lastQuote = line.lastIndexOf('"');

            if (firstQuote == -1 || lastQuote == firstQuote) {
                System.out.println("Malformed FILE response.");
                return;
            }
            String content = line.substring(firstQuote + 1, lastQuote);

            String escapedContent = line.substring(firstQuote + 1, lastQuote);

            content = content.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
            if (content.startsWith("\"") && content.endsWith("\"")) {
                content = content.substring(1, content.length() - 1);}

            String filename = "downloaded_file";

            Files.writeString(Path.of(filename), content);
            System.out.println("File saved as " + filename);

        } catch (IOException e) {
            System.out.println("Failed to write downloaded file.");
        }
    }

    private static void send(String msg) throws IOException {
        lastCommandSent = msg;
        serverWriter.write(msg);
        serverWriter.newLine();
        serverWriter.flush();
    }

    private static void printWelcome() {
        System.out.println("=".repeat(60) + "\n");
        System.out.println("                   JAVA CHAT CLIENT GUIDE");
        System.out.println("=".repeat(60));
        System.out.println(" 1. Register your username:  :username [your_username]");
        System.out.println(" 2. Create room:             :create [room_name]");
        System.out.println(" 3. Join room:               :join [room_name]");
        System.out.println(" 4. Send message:            type and enter you entered the room");
        System.out.println(" 5. Whisper:                 :whisper [other user] [message]");
        System.out.println(" 6. Send file:               :file [path/to/file]");
        System.out.println(" 7. Download file:           :download [file_name]");
        System.out.println(" 8. Display list of rooms:   :list");
        System.out.println(" 9. Leave room:              :leave");
        System.out.println("10. Terminate the client:    :quit");
        System.out.println("11. Clear terminal output:   :clear");
        System.out.println("12. Show this manual:        :help");
        System.out.println("=".repeat(60) + "\n");

        System.out.println("type :help to see manual again!");
    }

    private static void printPrefix() {
        if (currentRoom == null) {
            System.out.print("> ");
            return;
        }
        System.out.print("[" + currentRoom + "] >");
        System.out.flush();
    }

    private static void clearOutput() {
        System.out.print("\033[H\033[2J");
        System.out.flush();    }
}
