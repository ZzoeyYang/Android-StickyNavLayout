package online.sniper.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private static final int BUFF_SIZE = 1024; // 1K Byte

    public boolean zipFile(File resFile, File zipFile) {
        boolean flag = false;
        ZipOutputStream zipout = null;
        BufferedInputStream in = null;
        if (resFile != null && resFile.exists() && zipFile != null) {
            try {
                if (zipFile.exists()) {
                    zipFile.delete();
                } else {
                    if (!zipFile.getParentFile().exists()) {
                        zipFile.getParentFile().mkdirs();
                    }
                }
                zipFile.createNewFile();
                zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));
                byte buffer[] = new byte[BUFF_SIZE];
                in = new BufferedInputStream(new FileInputStream(resFile), BUFF_SIZE);
                int realLength;
                zipout.putNextEntry(new ZipEntry(resFile.getName()));
                while ((realLength = in.read(buffer)) != -1) {
                    zipout.write(buffer, 0, realLength);
                }
                zipout.flush();
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
                flag = false;
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (zipout != null) {
                        zipout.closeEntry();
                        zipout.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    public boolean zipFiles(File[] resFile, File zipFile) {
        boolean flag = false;

        for (File file : resFile) {
            if (file.exists()) {
                flag = true;
                break;
            }
        }
        if (!flag)
            return false;

        flag = false;
        ZipOutputStream zipout = null;
        BufferedInputStream in = null;
        if (resFile != null && zipFile != null) {
            try {
                if (zipFile.exists()) {
                    zipFile.delete();
                } else {
                    if (!zipFile.getParentFile().exists()) {
                        zipFile.getParentFile().mkdirs();
                    }
                }
                zipFile.createNewFile();
                zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));

                int realLength;
                byte buffer[] = new byte[BUFF_SIZE];

                for (File file : resFile) {
                    if (!file.exists())
                        continue;
                    zipout.putNextEntry(new ZipEntry(file.getName()));
                    in = new BufferedInputStream(new FileInputStream(file), BUFF_SIZE);
                    while ((realLength = in.read(buffer)) != -1) {
                        zipout.write(buffer, 0, realLength);
                    }
                    in.close();
                }

                zipout.flush();
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
                flag = false;
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (zipout != null) {
                        zipout.closeEntry();
                        zipout.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }
}
