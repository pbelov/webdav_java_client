package com.pbelov.java.webdav_client;


import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class Main {
    public static boolean DEBUG = false;

    public static void main(String[] args) {
        new Main(args);
    }

    private enum Mode {
        LIST,
        UPLOAD,
        DOWNLOAD,
    }

    private String from, to, host, user, pass;

    public Main(String[] args) {
        Utils.println("", "Webdav Java Client v.0.2");
        if (args == null || args.length == 0) {
            System.out.println("Usage\n");
            Utils.println("upload -from [from] -to [to] -host [protocol://host:port] -user [user] -pass [password]");
            Utils.println("download -from [from] -to [to] -host [protocol://host:port] -user [user] -pass [password]");
            Utils.println("list -from [from] -host [protocol://host:port] -user [user] -pass [password]");
            System.out.println("Usage: upload -from [from] -to [to] -host [protocol://host:port] -user [user] -pass [password]");
        } else {
            Mode mode = parseArgs(args);
            Sardine sardine = SardineFactory.begin();

            try {
                sardine.setCredentials(user, pass);
                go(mode, sardine);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                sardine.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void go(Mode mode, Sardine sardine) throws IOException {
        if (mode == Mode.LIST) {
            goList(sardine);
        } else if (mode == Mode.UPLOAD) {
            goUpload(sardine);
        } else if (mode == Mode.DOWNLOAD) {
            goDownload(sardine);
        } else {
            Utils.error("", "Wrong 1st parameter, only " + StringUtils.toString(Mode.values()) + " are possible");
        }
    }

    private void goList(Sardine sardine) throws IOException {
        Utils.println("List of " + host + "/" + from + ": \r\n");
        List<DavResource> list = sardine.list(host + "/" + from);
        for (DavResource davResource : list) {
            Utils.println(davResource.getName());
        }
    }

    private void goUpload(Sardine sardine) throws IOException {
        File[] files = getFiles();
        for (File file : files) {
            System.out.println(file.getName());
            byte[] bytes = Files.readAllBytes(file.toPath());
            sardine.put(host + "/" + to + "/" + file.getName(), bytes);
        }
    }

    private void goDownload(Sardine sardine) throws IOException {
        Utils.error("", "Is not implemented yet");
        List<DavResource> list = getListFrom(sardine, from);
        downloadList(sardine, list);
    }

    private void downloadList(Sardine sardine, List<DavResource> list) throws IOException {
        for (int i = 1; i < list.size(); i++) {
            DavResource davResource = list.get(i);
            Utils.println("Downloading " + davResource);
            final String resourceName = davResource.getName();
            if (!davResource.isDirectory()) {
                InputStream is = sardine.get(host + "/" + davResource.getPath());
                File targetFile = new File(FileUtils.WORKING_DIR, to);
                FileUtils.checkFile(targetFile);
                targetFile.mkdir();
                targetFile = new File(targetFile, resourceName);
                FileUtils.saveFile(is, targetFile);
            } else {
                List<DavResource> subList = getListFrom(sardine, davResource.getPath());
                downloadList(sardine, subList);
            }
        }
    }

    private List<DavResource> getListFrom(Sardine sardine, String dir) throws IOException {
        return sardine.list(host + "/" + dir);
    }

    private File[] getFiles() {
        return new File(from).listFiles();
    }

    private Mode parseArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
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

        try {
            return Mode.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            Utils.println("", "Wrong 1st parameter, only " + StringUtils.toString(Mode.values()) + " are possible");
            System.exit(-1);
            return null;
        }
    }
}
