package com.zolomon.eda095.lab3;

import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.Thread;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ConnectionThread extends Thread {
    private Socket socket;
    public ConnectionThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader reader  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            String prevLine;
            do {
                String line = reader.readLine();
                writer.write(line + "\n");
                writer.flush();
                prevLine = line;
            } while (prevLine != null && !prevLine.equals("/quit"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Disconnected: " + socket.getInetAddress().toString());
    }
}
