package com.pbelov.java.webdav_client;


import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static boolean DEBUG = false;

    public static void main(String[] args) {
        new Main(args);
    }

    private String from, to, host, user, pass;

    public Main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: -from [from] -to [to] -host [protocol://host:port] -user [user] -pass [password]");
        } else {
            parseArgs(args);
            Sardine sardine = SardineFactory.begin();
            try {
                sardine.setCredentials(user, pass);
                File[] files = getFiles();
                for (File file : files) {
                    System.out.println(file.getName());
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    sardine.put(host + "/" + to + "/" + file.getName(), bytes);
                }
                sardine.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File[] getFiles() {
        return new File(from).listFiles();
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-from":
                    from = args[i + 1];
                    break;
                case "-to":
                    to = args[i + 1];
                    break;
                case "-host":
                    host = args[i + 1];
                    break;
                case "-user":
                    user = args[i + 1];
                    break;
                case "-pass":
                    pass = args[i + 1];
                    break;
            }
        }
    }
}
