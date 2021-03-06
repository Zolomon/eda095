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
    ArrayList<URL> urls;

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

    public synchronized URL getUrl() {
        return this.urls.remove(urls.size()-1);
    }

    public void run() {
        System.out.println("Starting to download " + this.url);

        try {
            URL url = new URL(this.url);
            String contents = downloadHtml(url);
            //System.out.println(contents);
            this.urls = getLinks(url, contents);

            System.out.println("Creating " + numOfThreads + " threads.");

            Runner[] runners = new Runner[numOfThreads];
            for(int i = 0; i < runners.length; i++) {
                runners[i] = new Runner(this, this.path);
                runners[i].start();
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
