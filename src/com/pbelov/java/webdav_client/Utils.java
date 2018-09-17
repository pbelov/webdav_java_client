package com.pbelov.java.webdav_client;

import java.io.File;

public class Utils {
    private Utils() {
    }

    private static void checkFile(File file) {
        if (file == null) {
            return;
        } else {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
        }
    }

    public static void println(String str) {
        println("", str);
    }

    public static void println(String tag, String str) {
        System.out.println(Main.DEBUG ? tag + ": " + str : str);
    }

    public static void error(String tag, String str) {
        System.err.println(Main.DEBUG ? tag + ": " + str : str);
    }
}
