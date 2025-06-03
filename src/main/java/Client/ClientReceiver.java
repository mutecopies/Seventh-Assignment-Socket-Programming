package Client;

import java.io.BufferedReader;

public class ClientReceiver implements Runnable {
    private BufferedReader reader;

    public ClientReceiver(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String message = reader.readLine();
                if (message == null) break;
                System.out.println("[Server]: " + message);
            }
        } catch (Exception e) {
            System.out.println("Disconnected from chat.");
        }
    }
}
