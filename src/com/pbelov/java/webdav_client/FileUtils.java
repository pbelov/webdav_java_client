package com.pbelov.java.webdav_client;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Paths;

public class FileUtils {
    public static final File WORKING_DIR = new File(Paths.get("").toAbsolutePath().toString());
    private static final String TAG = "FileUtils";

    private FileUtils() {}

}
