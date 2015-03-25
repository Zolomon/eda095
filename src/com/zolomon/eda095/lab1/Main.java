package com.zolomon.eda095.lab1;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zol on 3/12/2015.
 */
public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: PdfFetcher <url>");
        }
        try {
            URL url = new URL(args[0]);
            String contents = downloadHtml(url);
            ArrayList<URL> urls = getLinks(url, contents);
            for (URL u : urls) {
                try {
                    downloadPdf(u);
                } catch (IOException e) {
                    System.out.println("Could not download: " + u.toString());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
//
//    private static ArrayList<URL> stringsToUrls(URL url, ArrayList<String> parsedLinks) {
//        parsedLinks.parallelStream()
//                .map((String l) -> {
//                    try {
//                        return new URL(l);
//                    } catch (MalformedURLException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                });
//
//        return null;
//    }

    private static ArrayList<URL> getLinks(URL baseurl, String contents) {
        //Pattern pattern = Pattern.compile("(^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");
        //Pattern pattern = Pattern.compile("<a\\b[^>]*href=\"[^>]*>(.*?)</a>");
        //Pattern linkregex = Pattern.compile("href=\"[^>]*\".*?>");
        Pattern pattern = Pattern.compile("href=\"(?<link>.*?)\"");
        //Pattern linkregex = Pattern.compile("href=\"(?<link>.*\\.pdf)\"");
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

            System.out.println(url);
//            try {
//                urls.add(new URL(url));
//            } catch (MalformedURLException e) {
//                System.err.println("Malformed url: " + url);
//            }
        }
        return urls;
    }

    private static String makeAbsolute(String baseurl, String url) {
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

    private static boolean valid(String url) {
        return !url.matches("javascript:.*|mailto:.*") && url.endsWith(".pdf");
    }

    private static String downloadHtml(URL url) {
        String encoding = "ISO-8859-1";
        URLConnection uc = null;
        try {
            uc = url.openConnection();

            String contentType = uc.getContentType();
            int encodingStart = contentType.indexOf("charset=");
            if (encodingStart != -1) {
                encoding = contentType.substring(encodingStart + 8);
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

    private static void downloadPdfs(URL[] urls) throws IOException {
        for (URL url : urls) downloadPdf(url);
    }

    private static String downloadPdf(URL url) throws IOException {
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
            filename = filename.substring(filename.lastIndexOf("/") + 1);
            try (FileOutputStream fout = new FileOutputStream(filename)) {
                fout.write(data);
                fout.flush();
                System.out.println("File saved: " + filename);
            }
        }
        return null;
    }
}
