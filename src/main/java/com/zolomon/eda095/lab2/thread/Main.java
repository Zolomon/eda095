package com.zolomon.eda095.lab2.thread;

import com.zolomon.eda095.lab2.runners.constructor.Runner;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

/**
 * Created by zol on 3/12/2015.
 */
public class Main {
    int numOfThreads;
    String url;
    String path;
    
    public Main(int numOfThreads, String url, String path) {
        this.numOfThreads = numOfThreads;
        this.url = url;
        this.path = path;
    }

    public static void main(String[] args) {
        System.out.println("Started");
        int numOfThreads = 5;

        if (args.length == 0) {
            System.out.println("Usage: PdfFetcher <url>");
        }
        
        new Main(numOfThreads, args[0], args[1]).run();
    }

    private void waitWhileRunning(Runner[] threads) {
        try {
            boolean isWaiting = true;
            while (isWaiting) {
                for(Runner r : threads) {
                    if (r == null || (r != null && !r.isAlive())) {
                        isWaiting = false;
                        break;
                    }
                }
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }
    
    public void run() {
        System.out.println("Starting to download " + this.url);

        try {
            URL url = new URL(this.url);
            String contents = downloadHtml(url);
            //System.out.println(contents);
            ArrayList<URL> urls = getLinks(url, contents);
            System.out.println("Url count: " + urls.size());
            Runner[] threads = new Runner[numOfThreads];

            int t = 0;
            int aliveThreads = 0;
            while(urls.size() > 0) {
                System.out.println("t: " + t);
                // Next thread
                t++;
                t = t % (numOfThreads - 1);
                if (threads[t] != null && threads[t].isAlive()) {
                    waitWhileRunning(threads);
                }
                System.out.println(t);
                // Wait while it's working
                // if (threads[t] != null && aliveThreads == numOfThreads) {
                //     waitWhileRunning(threads);
                //     if (urls.size() < aliveThreads) {
                //         aliveThreads--;
                //     }
                // }
                System.out.println("Creating a new thread to download: "+ urls.get(urls.size()-1));
                threads[t] = new Runner(urls.remove(urls.size()-1), path);
                threads[t].start();
                if (aliveThreads < numOfThreads)
                    aliveThreads++;

                if (aliveThreads >= numOfThreads - 1) {
                    waitWhileRunning(threads);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private  ArrayList<URL> getLinks(URL baseurl, String contents) {
        Pattern pattern = Pattern.compile("href=\"(?<link>.*?)\"");
        Matcher tagmatcher = pattern.matcher(contents);
        ArrayList<URL> urls = new ArrayList<>();
        while (tagmatcher.find()) {
            String url = tagmatcher.group().replaceFirst("href=\"", "").replaceFirst("\"", "");
            if (valid(url)) {
                try {
                    urls.add(new URL(makeAbsolute(baseurl.toString(), url)));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return urls;
    }

    private  String makeAbsolute(String baseurl, String url) {
        if (url.matches("http://.*")) {
            return url;
        }
        if (url.matches("/.*") && baseurl.matches(".*$[^/]")) {
            return baseurl + "/" + url;
        }
        if (url.matches("[^/].*") && baseurl.matches(".*[^/]")) {
            return baseurl + "/" + url;
        }
        if (url.matches("/.*") && baseurl.matches(".*[/]")) {
            return baseurl + url;
        }
        if (url.matches("/.*") && baseurl.matches(".*[^/]")) {
            return baseurl + url;
        }
        throw new RuntimeException("Cannot make the link absolute. Url: " + baseurl
                                   + " Link " + url);
    }

    private  boolean valid(String url) {
        return !url.matches("javascript:.*|mailto:.*") && url.endsWith(".pdf");
    }

    private  String downloadHtml(URL url) {
        String encoding = "ISO-8859-1";
        URLConnection uc = null;
        try {
            uc = url.openConnection();

            String contentType = uc.getContentType();
            if (contentType != null) {
                int encodingStart = contentType.indexOf("charset=");
                if (encodingStart != -1) {
                    encoding = contentType.substring(encodingStart + 8);
                }
            }
            InputStream in = new BufferedInputStream(uc.getInputStream());
            StringBuilder stringBuilder = new StringBuilder();
            try (Reader r = new InputStreamReader(in, encoding)) {
                    int c;
                    while ((c = r.read()) != -1) {
                        stringBuilder.append((char) c);
                    }
                }
            in.close();
            return stringBuilder.toString();
        } catch (MalformedURLException ex) {
            System.err.println(url.toString() + " is not a parseable URL");
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Server sent an encoding Java does not support: " + ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
