package Client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;

public class Client {
    private static String username;
    private static BufferedReader reader;
    private static BufferedWriter writer;
    private static InputStream inputStream;
    private static OutputStream outputStream;

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            Scanner scanner = new Scanner(System.in);

            // --- LOGIN PHASE ---
            System.out.println("===== Welcome to CS Music Room =====");
            boolean loggedIn = false;

            while (!loggedIn) {
                System.out.print("Username: ");
                username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();

                sendLoginRequest(username, password);
                String response = reader.readLine();

                if ("SUCCESS".equals(response)) {
                    System.out.println("Login successful!");
                    loggedIn = true;
                } else {
                    System.out.println("Login failed. Try again.");
                }
            }

            // --- ACTION MENU LOOP ---
            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> enterChat(scanner);
                    case "2" -> uploadFile(scanner);
                    case "3" -> requestDownload(scanner);
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Enter chat box");
        System.out.println("2. Upload a file");
        System.out.println("3. Download a file");
        System.out.println("0. Exit");
    }

    private static void sendLoginRequest(String username, String password) throws IOException {
        writer.write("LOGIN " + username + " " + password + "\n");
        writer.flush();
    }

    private static void enterChat(Scanner scanner) throws IOException {
        System.out.println("You have entered the chat. Type /exit to leave.");
        Thread receiver = new Thread(new ClientReceiver(reader));
        receiver.start();

        String message = "";
        while (!message.equalsIgnoreCase("/exit")) {
            message = scanner.nextLine();
            if (!message.equalsIgnoreCase("/exit")) {
                sendChatMessage(message);
            }
        }
        receiver.interrupt();
    }

    private static void sendChatMessage(String message) throws IOException {
        writer.write("CHAT " + message + "\n");
        writer.flush();
    }

    private static void uploadFile(Scanner scanner) throws IOException {
        File folder = new File("resources/Client/" + username);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No files to upload.");
            return;
        }

        System.out.println("Select a file to upload:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        System.out.print("Enter file number: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }

        File file = files[choice];
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        writer.write("UPLOAD " + file.getName() + " " + fileBytes.length + "\n");
        writer.flush();

        outputStream.write(fileBytes);
        outputStream.flush();

        System.out.println("File uploaded successfully.");
    }

    private static void requestDownload(Scanner scanner) throws IOException {
        writer.write("LISTFILES\n");
        writer.flush();

        String fileList = reader.readLine();
        String[] files = fileList.split(",");

        System.out.println("Available files:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i]);
        }

        System.out.print("Enter file number to download: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }

        String selectedFile = files[choice];
        writer.write("DOWNLOAD " + selectedFile + "\n");
        writer.flush();

        String metadata = reader.readLine();
        String[] parts = metadata.split(" ");
        String filename = parts[0];
        int length = Integer.parseInt(parts[1]);

        byte[] fileBytes = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int r = inputStream.read(fileBytes, bytesRead, length - bytesRead);
            if (r == -1) break;
            bytesRead += r;
        }

        File outFile = new File("resources/Client/" + username + "/" + filename);
        Files.write(outFile.toPath(), fileBytes);
        System.out.println("Downloaded file: " + filename);
    }
}
