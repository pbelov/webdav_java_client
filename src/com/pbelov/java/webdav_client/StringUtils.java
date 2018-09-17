package com.pbelov.java.webdav_client;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
    private StringUtils() {
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();

        return sdfDate.format(now);
    }

    static void handleException(String TAG, Exception e) {
        if (Main.DEBUG) {
            e.printStackTrace();
        } else {
            Utils.error(TAG, "Error: " + e.getMessage());
        }

        FileUtils.writeStringToFile(e.toString(), new File("stacktrace.log"));
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }
}
