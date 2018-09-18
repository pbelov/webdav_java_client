package com.pbelov.java.webdav_client;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static final File WORKING_DIR = new File(Paths.get("").toAbsolutePath().toString());
    private static final String TAG = "FileUtils";

    private FileUtils() {
    }

    public static void checkFile(File file) {
//        if (file.isFile()) {
        file.getParentFile().mkdirs();
//        }
    }

    public static void saveFile(InputStream is, File targetFile) throws IOException {
        Utils.println("Save file: " + targetFile.getAbsolutePath());
        FileUtils.checkFile(targetFile);
        if (targetFile.createNewFile()) {
            java.nio.file.Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
            IOUtils.closeQuietly(is);
    }

    public static List<File> getAllFilesFrom(File from) {
        List<File> files = new ArrayList<>();
        for (File file : from.listFiles()) {
            if (file.isFile()) {
                files.add(file);
            } else {
                files.addAll(getAllFilesFrom(file));
            }
        }


        return files;
    }

}
