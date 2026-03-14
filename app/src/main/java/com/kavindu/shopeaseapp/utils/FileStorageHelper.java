package com.kavindu.shopeaseapp.utils;

import android.content.Context;
import android.os.StatFs;
import java.io.*;

public class FileStorageHelper {

    // Write text to internal storage
    public static void writeToInternalStorage(Context ctx, String filename, String content) {
        try (FileOutputStream fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read text from internal storage
    public static String readFromInternalStorage(Context ctx, String filename) {
        try (FileInputStream fis = ctx.openFileInput(filename);
             InputStreamReader reader = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(reader)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString().trim();
        } catch (IOException e) {
            return null;
        }
    }

    // Append to a log file
    public static void appendToLog(Context ctx, String entry) {
        try (FileOutputStream fos = ctx.openFileOutput("app_log.txt",
                Context.MODE_PRIVATE | Context.MODE_APPEND);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(entry + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Delete file from internal storage
    public static boolean deleteFile(Context ctx, String filename) {
        return ctx.deleteFile(filename);
    }

    // List all internal files
    public static String[] listInternalFiles(Context ctx) {
        return ctx.fileList();
    }

    // Get storage info string
    public static String getStorageInfo(Context ctx) {
        StatFs stat = new StatFs(ctx.getFilesDir().getPath());
        long blockSize  = stat.getBlockSizeLong();
        long available  = stat.getAvailableBlocksLong() * blockSize;
        long total      = stat.getBlockCountLong() * blockSize;
        return String.format("Internal Storage — Free: %s / Total: %s",
                formatSize(available), formatSize(total));
    }

    private static String formatSize(long bytes) {
        if (bytes >= 1_000_000_000) return String.format("%.1f GB", bytes / 1e9);
        if (bytes >= 1_000_000)     return String.format("%.1f MB", bytes / 1e6);
        return String.format("%.1f KB", bytes / 1e3);
    }
}