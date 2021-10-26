package de.chaosschwein.chat.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) {

        new Thread(() -> {
            while (true) {
                try {
                    ServerSocket server = new ServerSocket(8888);
                    Socket client = server.accept();

                    ConnectionHandler ch = new ConnectionHandler(client);

                    ch.start();

                    server.close();
                } catch (IOException ignore) {
                }
            }
        }).start();
        System.out.println("Server Started!");
    }

}

class ConnectionHandler extends Thread {

    private static final HashMap<DataOutputStream, String> clientList = new HashMap<>();
    private final Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean permConn = false;

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {

        try {
            this.in = new DataInputStream(this.client.getInputStream());
            this.out = new DataOutputStream(this.client.getOutputStream());

            int len;
            byte[] data = new byte[4096];
            while ((len = in.read(data)) != -1) {
                String msg = new String(data, 0, len);

                if (msg.startsWith("[LOGIN]")) {
                    String channel = msg.replace("[LOGIN]", "").split(" ")[0];
                    String username = msg.replace("[LOGIN]", "").split(" ")[1];
                    for (DataOutputStream all : clientList.keySet()) {
                        if (clientList.get(all).equalsIgnoreCase(channel)) {
                            all.write(("[JOINMSG]" + username).getBytes());
                            all.flush();
                        }
                    }
                    permConn = true;
                    clientList.put(out, channel);
                }
                if (msg.startsWith("[GLOBALMSG]")) {
                    for (DataOutputStream all : clientList.keySet()) {
                        if (clientList.get(all).equalsIgnoreCase(msg.split("CHANNEL")[1])) {
                            all.write(msg.split("CHANNEL")[0].replace("CHANNEL", "").getBytes());
                            all.flush();
                        }
                    }
                    out.close();
                    client.close();
                }
                if (msg.startsWith("[LOGOUT]")) {
                    String channel = msg.replace("[LOGOUT]", "").split(" ")[0];
                    String username = msg.replace("[LOGOUT]", "").split(" ")[1];
                    for (DataOutputStream all : clientList.keySet()) {
                        if (clientList.get(all).equalsIgnoreCase(channel)) {
                            all.write(("[LOGOUTMSG]" + username).getBytes());
                            all.flush();
                        }
                    }
                }
            }
            if (permConn) {
                permConn = false;
                clientList.remove(out);
            }

        } catch (SocketTimeoutException | SocketException ey) {
            if (permConn) {
                permConn = false;
                clientList.remove(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
