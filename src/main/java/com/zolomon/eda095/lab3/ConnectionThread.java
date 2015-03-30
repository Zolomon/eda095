package com.zolomon.eda095.lab3;

import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.Thread;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class ConnectionThread extends Thread {
    private Socket socket;
    private Mailbox mailbox;
    private BlockingQueue<Message> subscription;

    public ConnectionThread(Socket socket, Mailbox mailbox) {
        this.mailbox = mailbox;
        this.socket = socket;
        this.subscription = new BlockingQueue<Message>();
        mailbox.subscribe(subscription);
    }

    public void run() {
        try {
            //BufferedReader reader  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //PrintWriter writer = new PrintWriter(socket.getOutputStream());
            String prevLine;
            do {
                //String line = reader.readLine();
                Message message = subscription.take();
                writer.write( message.getTimestamp() + " " +
                              message.getAuthor() + ": " +
                              message.getMessage() + "\n");
                writer.flush();
                prevLine = message.getMessage();
            } while (prevLine != null && !prevLine.equals("/quit"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Disconnected: " + socket.getInetAddress().toString());
    }
}
