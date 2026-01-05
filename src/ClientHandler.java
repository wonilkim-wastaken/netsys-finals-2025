import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientHandler {

    private static BufferedWriter serverWriter;
    private static volatile String lastCommandSent;

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

            // Server listener
            Thread listener = new Thread(() -> listenToServer(serverReader));
            listener.setDaemon(true);
            listener.start();

            // User input loop
            String input;
            while ((input = userInput.readLine()) != null) {
                String normalized = normalizeInput(input);
                if (normalized == null) {
                    continue;
                }

                send(normalized);
                eraseLastLine();

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

    /* ================= Helpers ================= */

    private static void listenToServer(BufferedReader serverReader) {
        try {
            String line;
            while ((line = serverReader.readLine()) != null) {

                if (line.equals("OK")) {
                    handleOk();
                    continue;
                }

                if (line.startsWith("ERR:")) {
                    System.out.println(line);
                    continue;
                }

                if (line.startsWith("FILE: ")) {
                    handleFileDownload(line);
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
        }
        else if (lastCommandSent.startsWith(":create ")) {
            String server = lastCommandSent.substring(8);
            System.out.println("Server " + server + " created");
        }
        else if (lastCommandSent.startsWith(":leave")) {
            System.out.println("You left the server");
        }
    }

    private static String normalizeInput(String input) {
        input = input.trim();
        if (input.isEmpty()) return null;

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

            // Last requested filename assumption
            // In a real protocol this should be explicit
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
}
