package com.infocomm.logpond_v2.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by DoAsInfinity on 8/30/2017.
 */

public class FileManager {

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getPublicDirPath(Context context) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.infocomm." + "Palmera" + "/" + "Palmera");
        Log.i("Path", file.getAbsolutePath());
        Log.e("Exist  Dir", String.valueOf(file.exists()));
        if (!file.exists()) {
            boolean result = file.mkdirs();
            Log.e("Create Dir", String.valueOf(result));
        }
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
        return file.getAbsolutePath();
    }

    public static String getPrivateDirPath(Context context) {
        // getExternalFilesDir(null) = Android/data/package_name/files/
        //File file = new File(context.getExternalFilesDir(null), context.getString(R.string.app_name));
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/.com.android.systemss/sys/com.mydreamsoft/" + "Palmera");
        Log.i("Path", file.getAbsolutePath());
        Log.e("Exist  Dir", String.valueOf(file.exists()));
        if (!file.exists()) {
            boolean result = file.mkdirs();
            Log.e("Create Dir", String.valueOf(result));
        }
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
        return file.getAbsolutePath();
    }

    public static String[] retrieveTextContentAsStringArray(Context context, String filePath) {
        String[] stringArray = null;
        File file = new File(filePath);
        if (file.exists()) {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\r\n");
                }
            } catch (Exception e1) {

            } finally {
                if (br != null)
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            if (sb.length() > 0) {
                stringArray = sb.toString().split("\r\n");
            }
        }
        return stringArray;
    }

    /*
    public static String getLatestEntryNoInPrivate(Context context, String dir){
        File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/" + dir);
        if(!privateExportDir.exists()) privateExportDir.mkdirs();
        int count = 1;
        String todayDate = CalendarUtil.getDateTimeFormatByDate("yyyyMMdd", Calendar.getInstance().getTime());
        String entryNo = todayDate + (count>9? count:"0" + count);
        String fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
        File file = new File(privateExportDir.getAbsolutePath(), fileName);
        while(file.exists()){
            count++;
            entryNo = todayDate + (count>9? count:"0" + count);
            fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
            file = new File(privateExportDir.getAbsolutePath(), fileName);
        }
        return entryNo;
    }
    */

    public static String getLatestLogponOutEntryNoInPrivate(Context context) {
        File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_logpond_out");
        if (!privateExportDir.exists()) privateExportDir.mkdirs();

        File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_logpond_out");
        if (!privateTransferDir.exists()) privateTransferDir.mkdirs();

        int count = 1;
        String todayDate = CalendarUtil.getDateTimeFormatByDate("yyyyMMdd", Calendar.getInstance().getTime());
        String entryNo = todayDate + (count > 9 ? count : "0" + count);
        String fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
        File exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
        File uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        while (exportFile.exists() || uploadedFile.exists()) {
            count++;
            entryNo = todayDate + (count > 9 ? count : "0" + count);
            fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
            exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
            uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        }
        return entryNo;
    }

    public static String getLatestLogponInEntryNoInPrivate(Context context) {
        File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_logpond_in");
        if (!privateExportDir.exists()) privateExportDir.mkdirs();

        File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_logpond_in");
        if (!privateTransferDir.exists()) privateTransferDir.mkdirs();

        int count = 1;
        String todayDate = CalendarUtil.getDateTimeFormatByDate("yyyyMMdd", Calendar.getInstance().getTime());
        String entryNo = todayDate + (count > 9 ? count : "0" + count);
        String fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
        File exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
        File uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        while (exportFile.exists() || uploadedFile.exists()) {
            count++;
            entryNo = todayDate + (count > 9 ? count : "0" + count);
            fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
            exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
            uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        }
        return entryNo;
    }

    public static String getLatestLogponPileEntryNoInPrivate(Context context) {
        File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_logpond_pile");
        if (!privateExportDir.exists()) privateExportDir.mkdirs();

        File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_logpond_pile");
        if (!privateTransferDir.exists()) privateTransferDir.mkdirs();

        int count = 1;
        String todayDate = CalendarUtil.getDateTimeFormatByDate("yyyyMMdd", Calendar.getInstance().getTime());
        String entryNo = todayDate + (count > 9 ? count : "0" + count);
        String fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
        File exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
        File uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        while (exportFile.exists() || uploadedFile.exists()) {
            count++;
            entryNo = todayDate + (count > 9 ? count : "0" + count);
            fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
            exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
            uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        }
        return entryNo;
    }

    public static String getLatestForestEntryNoInPrivate(Context context) {
        File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_forest");
        if (!privateExportDir.exists()) privateExportDir.mkdirs();

        File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_forest");
        if (!privateTransferDir.exists()) privateTransferDir.mkdirs();

        int count = 1;
        String todayDate = CalendarUtil.getDateTimeFormatByDate("yyyyMMdd", Calendar.getInstance().getTime());
        String entryNo = todayDate + (count > 9 ? count : "0" + count);
        String fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
        File exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
        File uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        while (exportFile.exists() || uploadedFile.exists()) {
            count++;
            entryNo = todayDate + (count > 9 ? count : "0" + count);
            fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
            exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
            uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        }
        return entryNo;
    }

    public static String getLatestBuyerGradingEntryNoInPrivate(Context context) {
        File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_buyer_grading");
        if (!privateExportDir.exists()) privateExportDir.mkdirs();

        File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_buyer_grading");
        if (!privateTransferDir.exists()) privateTransferDir.mkdirs();

        int count = 1;
        String todayDate = CalendarUtil.getDateTimeFormatByDate("yyyyMMdd", Calendar.getInstance().getTime());
        String entryNo = todayDate + (count > 9 ? count : "0" + count);
        String fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
        File exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
        File uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        while (exportFile.exists() || uploadedFile.exists()) {
            count++;
            entryNo = todayDate + (count > 9 ? count : "0" + count);
            fileName = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME) + "_" + entryNo + ".TXT";
            exportFile = new File(privateExportDir.getAbsolutePath(), fileName);
            uploadedFile = new File(privateTransferDir.getAbsolutePath(), fileName);
        }
        return entryNo;
    }

    public static JSONObject saveTempFile(Context context, String fileName, String data) {
        JSONObject jsonObject = new JSONObject();
        FileOutputStream fileOutputStream = null;
        try {
            // Save to private place
            File privateDir = new File(FileManager.getPrivateDirPath(context) + "/temp_folder");
            if (!privateDir.exists()) privateDir.mkdirs();
            File file = new File(privateDir, fileName);
            if (!file.exists()) file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes("UTF-8"));
            fileOutputStream.flush();
            fileOutputStream.close();

            System.out.println("~~~~~~~~~~~~~~~~~~Save temp file = " + file.getAbsolutePath());
            /*
            // Save to public place
            File publicDir = new File(FileManager.getPublicDirPath(context) + "/" + dir);
            if(!publicDir.exists()) publicDir.mkdirs();
            file = new File(publicDir, fileName);
            if(!file.exists()) file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes("UTF-8"));
            */

            jsonObject.put("name", file.getName());
            jsonObject.put("lastModified", file.lastModified());
            jsonObject.put("size", file.length());
            jsonObject.put("filePath", file.getAbsolutePath());

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject;
    }

    public static void removeTempFile(Context context, String fileName) {
        FileOutputStream fileOutputStream = null;
        try {
            // Remove from private place
            File privateDir = new File(FileManager.getPrivateDirPath(context) + "/temp_folder");
            if (!privateDir.exists()) privateDir.mkdirs();
            File file = new File(privateDir, fileName);
            if (file.exists()) file.delete();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object[] retrieveTextContentAsStringArrayFromTempFile(Context context, String fileName) {
        Object[] objectArray = null;
        File privateDir = new File(FileManager.getPrivateDirPath(context) + "/temp_folder");
        if (!privateDir.exists()) privateDir.mkdirs();
        File file = new File(privateDir, fileName);
        int position = -2;
        if (file.exists()) {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (position == -2) {
                        position = Integer.parseInt(line);
                    } else {
                        sb.append(line);
                        sb.append("\r\n");
                    }

                }
            } catch (Exception e1) {

            } finally {
                if (br != null)
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            if (sb.length() > 0) {
                objectArray = new Object[2];
                String[] dataArray = sb.toString().split("\r\n");
                objectArray[0] = position;
                objectArray[1] = dataArray;
            }
        }
        return objectArray;
    }

    public static JSONObject saveFile(Context context, String dir, String fileName, String data) {
        JSONObject jsonObject = new JSONObject();
        FileOutputStream fileOutputStream = null;
        try {
            // Save to private place
            File privateDir = new File(FileManager.getPrivateDirPath(context) + "/" + dir);
            if (!privateDir.exists()) privateDir.mkdirs();
            File file = new File(privateDir, fileName);
            if (!file.exists()) file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes("UTF-8"));
            fileOutputStream.flush();
            fileOutputStream.close();

            // Save to public place
            File publicDir = new File(FileManager.getPublicDirPath(context) + "/" + dir);
            if (!publicDir.exists()) publicDir.mkdirs();
            file = new File(publicDir, fileName);
            if (!file.exists()) file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes("UTF-8"));

            jsonObject.put("name", file.getName());
            jsonObject.put("lastModified", file.lastModified());
            jsonObject.put("size", file.length());
            jsonObject.put("filePath", file.getAbsolutePath());

            if (dir.toUpperCase().contains("LOGPOND_IN")) {
                removeTempFile(context, "LOGPOND_IN.TXT");
            } else if (dir.toUpperCase().contains("LOGPOND_OUT")) {
                removeTempFile(context, "LOGPOND_OUT.TXT");
            } else if (dir.toUpperCase().contains("LOGPOND_PILE")) {
                removeTempFile(context, "LOGPOND_PILE.TXT");
            } else if (dir.toUpperCase().contains("BUYER_GRADING")) {
                removeTempFile(context, "BUYER_GRADING.TXT");
            }
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject;
    }

    public static int getPublicFileCount(Context context, String dir) {
        File publicDir = new File(FileManager.getPublicDirPath(context) + "/" + dir);
        if (!publicDir.exists()) publicDir.mkdirs();
        int count = 0;
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        File[] files = publicDir.listFiles(fileFilter);
        return files.length;
    }

    public static File[] getPublicFile(Context context, String dir) {
        File publicDir = new File(FileManager.getPublicDirPath(context) + "/" + dir);
        if (!publicDir.exists()) publicDir.mkdirs();
        int count = 0;
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        File[] files = publicDir.listFiles(fileFilter);
        return files;
    }

    public static File[] getPublicFileWithoutDeletedFiles(Context context, String dir) {
        File publicDir = new File(FileManager.getPublicDirPath(context) + "/" + dir);
        if (!publicDir.exists()) publicDir.mkdirs();
        int count = 0;

        File[] files = publicDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.toUpperCase().endsWith("D.TXT") && !name.endsWith("D.txt");
            }
        });

        return files;
    }

    public static File[] getPrivateFileWithoutDeletedFiles(Context context, String dir) {
        File privateDir = new File(FileManager.getPrivateDirPath(context) + "/" + dir);
        if (!privateDir.exists()) privateDir.mkdirs();
        int count = 0;

        File[] files = privateDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.toUpperCase().endsWith("D.TXT") && !name.endsWith("D.txt");
            }
        });

        return files;
    }

    public static void moveFile(File file, File dir) {
        if (!dir.exists()) dir.mkdirs();
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (inputChannel != null)
                    inputChannel.close();
                if (outputChannel != null) outputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static boolean isAndroidDBFolderEmpty(Context context) {
        File androidDbDir = new File(FileManager.getPrivateDirPath(context) + "/android_db/" + SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME));
        if (!androidDbDir.exists()) androidDbDir.mkdirs();
        int count = 0;
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        File[] files = androidDbDir.listFiles(fileFilter);
        return files.length == 0;
    }

    public static void removeAndroidDBFolderEmpty(Context context) {
        File androidDbDir = new File(FileManager.getPrivateDirPath(context) + "/android_db/" + SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME));
        if (!androidDbDir.exists()) androidDbDir.mkdirs();
        removeDirectory(androidDbDir);
    }

    public static void generateSummaryFile(Context context, String dirPath) {
        final String username = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.USERNAME);
        File sourceFile = new File(FileManager.getPrivateDirPath(context), dirPath);
        if (!sourceFile.exists()) sourceFile.mkdirs();
        File[] files = sourceFile.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return (pathname.getName().toUpperCase().endsWith(".TXT")
                        || pathname.isDirectory())
                        && !pathname.getName().toUpperCase().endsWith("D.TXT")
                        && pathname.getName().toUpperCase().startsWith(username + "_");
            }
        });
        StringBuffer sb = new StringBuffer();
        String nextDate = "";
        int count = 0;
        int totalBatch = 0;
        if (files != null) {
            files = sortByNumber(files, false);
            //System.out.println("filelength=" +  files.length);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                int s = fileName.indexOf('_') + 1;
                int e = fileName.lastIndexOf('.');
                String date = fileName.substring(s, e);
                date = date.substring(0, 8);
                //System.out.println("file =" +  file.getName());
                //System.out.println("date =" +  date);
                if ((i + 1) < files.length) {
                    file = files[i + 1];
                    fileName = file.getName();
                    s = fileName.indexOf('_') + 1;
                    e = fileName.lastIndexOf('.');
                    nextDate = fileName.substring(s, e);
                    nextDate = nextDate.substring(0, 8);
                }
                // Is Last File || Date not same with next date
                if ((i + 1) == files.length || !nextDate.equalsIgnoreCase(date)) {
                    //if((i + 1)!=files.length)
                    count++;
                    totalBatch++;
                    sb.append(date + "^" + count + "\r\n");
                    count = 0;
                } else {
                    count++;
                }
            }
        }


        String data = totalBatch + "\r\n" + sb.toString();
        FileOutputStream fileOutputStream = null;
        try {
            File privateDir = new File(FileManager.getPrivateDirPath(context) + "/export_summary");
            if (!privateDir.exists()) privateDir.mkdirs();
            String summaryFileName = username.toUpperCase() + "_" + dirPath.toLowerCase().replace("_", "") + ".txt";
            if (summaryFileName.toLowerCase().contains("transfer"))
                summaryFileName = summaryFileName.replace("transfer", "");
            File privateFile = new File(FileManager.getPrivateDirPath(context) + "/export_summary/" + summaryFileName.toUpperCase());
            if (!privateFile.exists()) privateFile.createNewFile();
            fileOutputStream = new FileOutputStream(privateFile);
            fileOutputStream.write(data.getBytes("UTF-8"));

            /*
            fileOutputStream.close();
            File publicDir = new File(FileManager.getPublicDirPath(context) + "/export_summary");
            if(!publicDir.exists()) publicDir.mkdirs();
            File publiFile= new File(FileManager.getPublicDirPath(context) + "/export_summary/" + summaryFileName.toUpperCase());
            if(!publiFile.exists()) publiFile.createNewFile();
            fileOutputStream = new FileOutputStream(publiFile);
            fileOutputStream.write(data.getBytes("UTF-8"));
            */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        System.out.println(dirPath + ":" + data);
    }

    public static File[] sortByNumber(File[] files, final boolean isAsc) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                if (isAsc) return n1 - n2;
                else return n2 - n1;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    int s = name.indexOf('_') + 1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch (Exception e) {
                    i = 0; // if filename does not match the format
                    // then default to 0
                }
                return i;
            }
        });
        return files;
    }

    public static boolean removeDirectory(File directory) {

        // System.out.println("removeDirectory " + directory);

        if (directory == null)
            return false;
        if (!directory.exists())
            return true;
        if (!directory.isDirectory())
            return false;

        String[] list = directory.list();

        // Some JVMs return null for File.list() when the
        // directory is empty.
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File entry = new File(directory, list[i]);

                //        System.out.println("\tremoving entry " + entry);

                if (entry.isDirectory()) {
                    if (!removeDirectory(entry))
                        return false;
                } else {
                    if (!entry.delete())
                        return false;
                }
            }
        }

        return directory.delete();
    }

    public static void removeUnnessaryFolder(Context context) {
        // Remove public folder
        File publicDir = new File(FileManager.getPublicDirPath(context) + "/transfer_logpond_in");
        if (!publicDir.exists()) publicDir.mkdirs();
        FileManager.removeDirectory(publicDir.getAbsoluteFile());

        publicDir = new File(FileManager.getPublicDirPath(context) + "/transfer_logpond_out");
        if (!publicDir.exists()) publicDir.mkdirs();
        FileManager.removeDirectory(publicDir.getAbsoluteFile());

        publicDir = new File(FileManager.getPublicDirPath(context) + "/transfer_logpond_pile");
        if (!publicDir.exists()) publicDir.mkdirs();
        FileManager.removeDirectory(publicDir.getAbsoluteFile());

        publicDir = new File(FileManager.getPublicDirPath(context) + "/transfer_buyer_grading");
        if (!publicDir.exists()) publicDir.mkdirs();
        FileManager.removeDirectory(publicDir.getAbsoluteFile());

        publicDir = new File(FileManager.getPublicDirPath(context) + "/export_summary");
        if (!publicDir.exists()) publicDir.mkdirs();
        FileManager.removeDirectory(publicDir.getAbsoluteFile());
    }
}
