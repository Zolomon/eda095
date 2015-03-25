package com.zolomon.eda095.lab2.constructor;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by zol on 3/25/2015.
 */
public class Runner {
    private URL url;
    private String path;

    public Runner(URL url, String path) {
        this.url = url;
        this.path = path;
    }
    public void run() throws IOException {

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
            }
    }
}
