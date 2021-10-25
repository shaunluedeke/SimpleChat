package de.chaosschwein.chat.main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) {

        new Thread(()->{
            while(true) {
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
    }

}

class ConnectionHandler extends Thread{

    private final Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private static ArrayList<DataOutputStream> clientList = new ArrayList<>();
    private boolean permConn=false;

    public ConnectionHandler(Socket client){
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
                    permConn = true;
                    clientList.add(out);
                }
                if (msg.startsWith("[GLOBALMSG]")) {
                    for (DataOutputStream all : clientList) {
                        all.write(msg.getBytes());
                        all.flush();
                    }
                    out.close();
                    client.close();
                }
            }
            if (permConn) {
                permConn = false;
                clientList.remove(out);
            }

        }catch(SocketTimeoutException | SocketException ey) {
            if (permConn) {
                permConn = false;
                clientList.remove(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
