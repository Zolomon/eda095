package com.zolomon.eda095.lab2.runners.constructor;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import com.zolomon.eda095.lab2.thread.Main;
import java.net.URLConnection;

/**
 * Created by zol on 3/25/2015.
 */
public class Runner extends Thread {
    private Main main;
    private String path;

    public Runner(Main main, String path) {
        this.main = main;
        this.path = path;
    }

    public void run() {
        boolean isRunning = true;
        while (isRunning) {
            try {
                URL  url = main.getUrl();
                if (url == null) {
                    isRunning = false;
                    System.out.println("No more URLs to download");
                    return;
                }
                System.out.println("Current URL: " + url.toString());
                URLConnection con = url.openConnection();
                String contentType = con.getContentType();
                int contentLength = con.getContentLength();
                if (contentType.startsWith("text/") || contentLength == -1) {
                    throw new IOException("This is not a binary file");
                }

                try (InputStream raw = con.getInputStream()) {
                        InputStream in = new BufferedInputStream(raw);
                        byte[] data = new byte[contentLength];
                        int offset = 0;
                        while (offset < contentLength) {
                            int bytesRead = in.read(data, offset, data.length - offset);
                            if (bytesRead == -1) break;
                            offset += bytesRead;
                            //System.out.println("Read: " + bytesRead);
                        }

                        if (offset != contentLength) {
                            throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes in total");
                        }

                        String filename = url.getFile();
                        filename = path + filename.substring(filename.lastIndexOf("/") + 1);
                        try (FileOutputStream fout = new FileOutputStream(filename)) {
                                fout.write(data);
                                fout.flush();
                                System.out.println("File saved: " + filename);
                            }
                    } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}
