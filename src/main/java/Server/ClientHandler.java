package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private List<ClientHandler> allClients;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> allClients) {
        this.socket = socket;
        this.allClients = allClients;

        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error initializing client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = reader.readLine();
                if (msg == null) break;

                if (msg.startsWith("LOGIN")) {
                    String[] tokens = msg.split(" ");
                    if (tokens.length == 3) {
                        handleLogin(tokens[1], tokens[2]);
                    }
                } else if (msg.startsWith("CHAT")) {
                    String chatMessage = msg.substring(5);
                    broadcast(chatMessage);
                } else if (msg.startsWith("UPLOAD")) {
                    String[] parts = msg.split(" ");
                    String filename = parts[1];
                    int fileLength = Integer.parseInt(parts[2]);
                    receiveFile(filename, fileLength);
                } else if (msg.startsWith("LIST_FILES")) {
                    sendFileList();
                } else if (msg.startsWith("DOWNLOAD")) {
                    String fileName = msg.substring(9).trim();
                    sendFile(fileName);
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            allClients.remove(this);
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket.");
            }
        }
    }

    private void sendMessage(String msg) {
        try {
            writer.write(msg);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to " + username);
        }
    }

    private void broadcast(String msg) {
        for (ClientHandler client : allClients) {
            if (client != this) {
                client.sendMessage(msg);
            }
        }
    }

    private void sendFileList() {
        File folder = new File("resources/Server/");
        File[] files = folder.listFiles();
        StringBuilder fileList = new StringBuilder();

        if (files != null) {
            for (File file : files) {
                fileList.append(file.getName()).append(",");
            }
        }

        sendMessage(fileList.toString().replaceAll(",$", ""));
    }

    private void sendFile(String fileName) {
        try {
            File file = new File("resources/Server/" + fileName);
            if (!file.exists()) {
                sendMessage("ERROR File not found.");
                return;
            }

            sendMessage("FILE_START " + file.getName() + " " + file.length());
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            fis.close();
        } catch (IOException e) {
            System.out.println("Failed to send file: " + fileName);
        }
    }

    private void receiveFile(String filename, int fileLength) {
        try {
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[fileLength];
            int totalRead = 0;
            while (totalRead < fileLength) {
                int bytesRead = is.read(buffer, totalRead, fileLength - totalRead);
                if (bytesRead == -1) break;
                totalRead += bytesRead;
            }

            saveUploadedFile(filename, buffer);
            System.out.println("Received file: " + filename);
        } catch (IOException e) {
            System.out.println("Error receiving file: " + filename);
        }
    }

    private void saveUploadedFile(String filename, byte[] data) throws IOException {
        File dir = new File("resources/Server/");
        if (!dir.exists()) dir.mkdirs();

        FileOutputStream fos = new FileOutputStream(new File(dir, filename));
        fos.write(data);
        fos.close();
    }

    private void handleLogin(String username, String password) throws IOException {
        boolean valid = Server.authenticate(username, password);
        if (valid) {
            this.username = username;
            sendMessage("LOGIN_SUCCESS");
            System.out.println(username + " logged in.");
        } else {
            sendMessage("LOGIN_FAILED");
        }
    }
}
