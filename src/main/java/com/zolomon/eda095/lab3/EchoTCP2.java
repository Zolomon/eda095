package com.zolomon.eda095.lab3;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

public  class EchoTCP2 {
    public EchoTCP2() {}

    public static void main(String[] args) {
        int port = 30000;
        try {
            ServerSocket ss = new ServerSocket(port);

            Socket socket;
            while((socket = ss.accept()) != null) {
                InetAddress ia = socket.getInetAddress();
                System.out.println("Connected client from: " + ia.toString());
                new ConnectionThread(socket).start();
            }
        }
        catch (IOException e) {
                e.printStackTrace();
        }
    }
}
