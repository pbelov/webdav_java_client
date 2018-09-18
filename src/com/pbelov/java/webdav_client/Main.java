package com.pbelov.java.webdav_client;


import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

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

    private final String pattern = Pattern.quote(System.getProperty("file.separator"));
    private final String WEB_SEPARATOR = "/";


    public Main(String[] args) {
        Utils.println("", "Webdav Java Client v.0.2.2");
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
        Utils.println("List of " + host + WEB_SEPARATOR + from + ": \r\n");
        List<DavResource> list = sardine.list(host + WEB_SEPARATOR + from);
        for (DavResource davResource : list) {
            Utils.println(davResource.getName());
        }
    }

    private void goUpload(Sardine sardine) throws IOException {
        File[] files = getFiles();
        uploadFiles(sardine, files);
    }

    private void uploadFiles(Sardine sardine, File[] localFiles) throws IOException {
        if (localFiles != null) {
            for (File localFile : localFiles) {
                if (localFile.isFile()) {
                    uploadFile(sardine, localFile);
                } else {
                    uploadFiles(sardine, localFile.listFiles());
                }
            }
        }
    }

    private void uploadFile(Sardine sardine, File localFile) throws IOException {
        Utils.println("Upload: " + localFile);
        byte[] bytes = Files.readAllBytes(localFile.toPath());
        createRemoteDirectory(sardine, host + WEB_SEPARATOR + to);
        createRemoteDirectories(sardine, localFile.getParentFile().getPath());

        sardine.put(host + WEB_SEPARATOR + to + WEB_SEPARATOR + localFile.getParent().replaceAll(pattern, WEB_SEPARATOR) + WEB_SEPARATOR + localFile.getName(), bytes);
    }

    private void createRemoteDirectory(Sardine sardine, String path) throws IOException {
        if (!sardine.exists(path)) {
            sardine.createDirectory(path);
        }
    }

    private void createRemoteDirectories(Sardine sardine, String path) throws IOException {
        String[] pathDirs = path.split(pattern);
        StringBuilder sb = new StringBuilder();
        for (String pathDir : pathDirs) {
            sb.append(pathDir);
            String remoteDirPath = host + WEB_SEPARATOR + to + WEB_SEPARATOR + sb.toString();
            createRemoteDirectory(sardine, remoteDirPath);
            sb.append(WEB_SEPARATOR);
        }
    }

    private void goDownload(Sardine sardine) throws IOException {
        List<DavResource> list = getListFrom(sardine, from);
        downloadList(sardine, list);
    }

    private void downloadList(Sardine sardine, List<DavResource> list) throws IOException {
        for (int i = 1; i < list.size(); i++) {
            DavResource davResource = list.get(i);
            Utils.println("Downloading " + davResource);
            final String resourceName = davResource.getName();
            final String resourcePath = davResource.getPath();
            if (!davResource.isDirectory()) {
                InputStream is = sardine.get(host + WEB_SEPARATOR + resourcePath);
                File targetFile = new File(FileUtils.WORKING_DIR, to);
                FileUtils.checkFile(targetFile);
                targetFile.mkdir();
                String targetPath = targetFile.getPath() + File.separator + davResource.getPath().substring(from.length() + 2).replaceAll(WEB_SEPARATOR, "\\" + File.separator).replaceAll(resourceName, "");
                targetFile = new File(targetPath, resourceName);
                FileUtils.saveFile(is, targetFile);
            } else {
                List<DavResource> subList = getListFrom(sardine, resourcePath);
                downloadList(sardine, subList);
            }
        }
    }

    private List<DavResource> getListFrom(Sardine sardine, String dir) throws IOException {
        return sardine.list(host + WEB_SEPARATOR + dir);
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
