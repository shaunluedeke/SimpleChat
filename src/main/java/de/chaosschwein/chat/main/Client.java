package de.chaosschwein.chat.main;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class Client {
    public static String username;
    public static String channel;

    public static void main(String[] args) {

        username = JOptionPane.showInputDialog(null, "Your Username:").replace(" ", "_");

        try {
            if (username.replace(" ", "").length() < 1) {
                System.exit(0);
            }

        } catch (NullPointerException e) {
            System.exit(0);
        }

        channel = JOptionPane.showInputDialog(null, "Chat Channel:").replace(" ", "_");

        try {
            if (channel.replace(" ", "").length() < 1) {
                System.exit(0);
            }

        } catch (NullPointerException e) {
            System.exit(0);
        }

        JFrame fenster = new JFrame();
        fenster.setTitle("Crypt Chat: " + channel);
        fenster.setSize(300, 400);
        fenster.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        fenster.setResizable(false);
        fenster.setLayout(null);
        fenster.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(fenster,
                        "Are you sure you want to close this window?", "Close Window?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    onclose();
                }
            }
        });

        JTextArea textArea = new JTextArea();
        textArea.setBounds(0, 0, 300, 330);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(0, 0, 300, 330);

        JTextField eingabe = new JTextField();
        eingabe.setBounds(0, 330, 210, 30);

        JButton sendBtn = new JButton("Send");
        sendBtn.setBounds(210, 330, 90, 30);

        fenster.add(sendBtn);
        fenster.add(eingabe);
        fenster.add(scroll);

        fenster.setVisible(true);

        sendBtn.addActionListener(e -> {
            if (eingabe.getText().replace(" ", "").length() > 0) {
                FormatText ft = new FormatText(eingabe.getText());

                SendMsg sendMsg = new SendMsg(username, ft.getText(), channel);
                sendMsg.start();
                eingabe.setText("");
            }
        });

        Connect connect = new Connect(textArea);
        connect.start();
    }

    public static void onclose() {
        try {
            Socket client = new Socket("185.117.3.153", 8888);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            String globalmsg = "[LOGOUT]" + channel + " " + username;

            out.write(globalmsg.getBytes());
            out.flush();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);

    }

}

class SendMsg extends Thread {
    private final String username;
    private final String text;
    private final String channel;

    public SendMsg(String username, String text, String channel) {
        this.username = username;
        this.text = text;
        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            Socket client = new Socket("185.117.3.153", 8888);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            String globalmsg = "[GLOBALMSG]" + username + ":\n" + text + " CHANNEL" + channel;

            out.write(globalmsg.getBytes());
            out.flush();

        } catch (IOException ignore) {
        }
    }
}

class FormatText {
    private final String text;

    public FormatText(String text) {
        this.text = text;
    }

    public String getText() {
        String formatText = "";
        String[] k = text.split("");

        int c = 0;

        for (int i = 0; i < k.length; i++) {
            c++;
            if (c == 32) {
                c = 0;
                k[i] = k[i] + "\n";
            }
            formatText += k[i];
        }
        HashMap<String, String> sonderzeichen = new HashMap<>() {{
            put("ä", ":ae:");
            put("ö", ":oe:");
            put("ü", ":ue:");
            put("ß", ":ss:");
            put("Ä", ":AE:");
            put("Ö", ":OE:");
            put("Ü", ":UE:");
        }};
        for (String key : sonderzeichen.keySet()) {
            formatText = formatText.replace(key, sonderzeichen.get(key));
        }
        return formatText+" ";
    }
}

class Connect extends Thread {

    private final JTextArea textArea;
    HashMap<String, String> sonderzeichen = new HashMap<>() {{
        put(":ae:", "ä");
        put(":oe:", "ö");
        put(":ue:", "ü");
        put(":ss:", "ß");
        put(":AE:", "Ä");
        put(":OE:", "Ö");
        put(":UE:", "Ü");
    }};

    public Connect(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void run() {
        try {
            Socket client = new Socket("185.117.3.153", 8888);
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            int len;
            byte[] data = new byte[4096];

            out.write(("[LOGIN]"+Client.channel+" "+Client.username).getBytes());
            out.flush();

            while ((len = in.read(data)) != -1) {
                String msg = new String(data, 0, len);

                if (msg.startsWith("[GLOBALMSG]")) {

                    String txt = msg.replace("[GLOBALMSG]", "").split("CHANNEL")[0];
                    for (String key : sonderzeichen.keySet()) {
                        txt = txt.replace(key, sonderzeichen.get(key));
                    }
                    textArea.setText(textArea.getText() + "\n" + txt);
                }
                if (msg.startsWith("[JOINMSG]")) {
                    String joinname = msg.replace("[JOINMSG]", "");
                    textArea.setText(textArea.getText() + "\n" + "* " + joinname + " joined the Chat *");
                }
                if (msg.startsWith("[LEAVEMSG]")) {
                    String joinname = msg.replace("[LEAVEMSG]", "");
                    textArea.setText(textArea.getText() + "\n" + "* " + joinname + " leaved the Chat *");
                }
            }
        } catch (IOException ignore) {
        }
    }
}
