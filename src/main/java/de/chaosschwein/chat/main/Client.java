package de.chaosschwein.chat.main;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client{

    public static void main(String[] args){

        String username = JOptionPane.showInputDialog(null,"Gib einen Username ein:");

        try {
            if (username.replace(" ", "").length() < 1) {
                System.exit(0);
            }

        }catch(NullPointerException e){
            System.exit(0);
        }

        JFrame fenster = new JFrame();
        fenster.setTitle("Crypt Chat");
        fenster.setSize(300,400);
        fenster.setDefaultCloseOperation(3);
        fenster.setResizable(false);
        fenster.setLayout(null);

        JTextArea textArea = new JTextArea();
        textArea.setBounds(0,0,300,330);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(0,0,300,330);

        JTextField eingabe = new JTextField();
        eingabe.setBounds(0,330,210,30);

        JButton sendBtn = new JButton("Send");
        sendBtn.setBounds(210,330,90,30);

        fenster.add(sendBtn);
        fenster.add(eingabe);
        fenster.add(scroll);

        fenster.setVisible(true);

        sendBtn.addActionListener(e->{
            if(eingabe.getText().replace(" ","").length() > 0){
                    FormatText ft = new FormatText(eingabe.getText());

                    SendMsg sendMsg = new SendMsg(username, ft.getText());
                    sendMsg.start();
                    eingabe.setText("");
            }
        });

        Connect connect = new Connect(username,textArea);
        connect.start();
    }

}

class SendMsg extends Thread{
    private final String username;
    private final String text;
    public SendMsg(String username,String text){
        this.username=username;
        this.text=text;
    }

    @Override
    public void run() {
        try {
            Socket client = new Socket("185.117.3.153",8888);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            String globalmsg = "[GLOBALMSG]"+username+":\n"+text;

            out.write(globalmsg.getBytes());
            out.flush();

        } catch (IOException ignore) {}
    }
}

class FormatText{
    private String text;
    public FormatText(String text){
        this.text = text;
    }
    public String getText(){
        String formatText="";
        String[] k = text.split(" ");

        int c = 0;

        for(int i=0;i<k.length;i++){
            c++;
            if(c==10){
                c=0;
                k[i]=k[i]+"\n";
            }
            formatText+=k[i]+" ";
        }
        return formatText;
    }
}

class Connect extends Thread{

    private final String username;
    private final JTextArea textArea;

    public Connect(String username,JTextArea textArea){
        this.username = username;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        try {
            Socket client = new Socket("185.117.3.153",8888);
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            int len;
            byte[] data = new byte[4096];

            out.write("[LOGIN]".getBytes());
            out.flush();

            while ((len = in.read(data)) != -1) {
                String msg = new String(data, 0, len);

                if(msg.startsWith("[GLOBALMSG]")){
                    textArea.setText(textArea.getText()+"\n"+msg.replace("[GLOBALMSG]",""));
                }

            }
        } catch (IOException ignore) {ignore.printStackTrace();}
    }
}
