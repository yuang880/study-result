package com.zhangxd.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.*;

public class ZipHelper {
    private static final int BUFFER_SIZE = 1024;
    private static final String PATH_SEP = "/";
    private static final String ZIP_SUFFIX = ".zip";


    public static void zipCompress(String sourceFileName, String destFileName) throws IOException {
        File sourceFile = new File(sourceFileName);
        File destFile = new File(destFileName);
        zipCompress(sourceFile, destFile);
    }
    public static void zipCompress(File sourceFile, File destFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destFile); CheckedOutputStream cos = new CheckedOutputStream(fos, new CRC32()); ZipOutputStream zos = new ZipOutputStream(cos)) {
            zipCompress(sourceFile, zos, "");
            zos.flush();
            zos.finish();
        }
    }

    private static void zipCompress(File sourceFile, ZipOutputStream zos, String basePath) throws IOException {
        if (sourceFile.isDirectory()) {
            zipCompressDir(sourceFile, zos, basePath);
        } else {
            zipCompressFile(sourceFile, zos, basePath);
        }
    }


    private static void zipCompressDir(File dir, ZipOutputStream zos, String basePath) throws IOException {
        File[] files = dir.listFiles();
        String newBasePath = basePath + dir.getName() + PATH_SEP;

        if (files.length <= 0) {
            ZipEntry entry = new ZipEntry(newBasePath);
            zos.putNextEntry(entry);
            zos.closeEntry();
        }
        for (File file : files) {
            zipCompress(file, zos, newBasePath);
        }
    }

    private static void zipCompressFile(File file, ZipOutputStream zos, String basePath) throws IOException {
        String entryName = basePath + file.getName();
        FileInputStream fis = new FileInputStream(file);
        zipCompress(fis, zos, entryName);
    }

    public static byte[] zipCompress(InputStream is, String entryName) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CheckedOutputStream cos = new CheckedOutputStream(baos, new CRC32());
             ZipOutputStream zos = new ZipOutputStream(cos)) {

            zos.setMethod(ZipOutputStream.DEFLATED);
            zipCompress(is, zos, entryName);
            zos.flush();
            zos.finish();
            byte[] result = baos.toByteArray();
            return result;
        }
    }

    private static void zipCompress(InputStream is, ZipOutputStream zos, String entryName) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            int count;
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                count = bis.read(buffer, 0, BUFFER_SIZE);
                if (count < 0) {
                    break;
                }
                zos.write(buffer, 0, count);
            }
            zos.closeEntry();
        }
    }

    public static void zipDecompress(String sourceFileName, String destDir) throws IOException {
        zipDecompress(new File(sourceFileName), new File(destDir));
    }

    public static void zipDecompress(File sourceFile, File destDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile)) {
            zipDecompress(fis, destDir);
        }
    }

    public static void zipDecompress(byte[] sourceBytes, File destDir) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(sourceBytes)) {
            zipDecompress(bais, destDir);
        }
    }

    public static void zipDecompress(InputStream is, File destFile) throws IOException {
        try (CheckedInputStream cis = new CheckedInputStream(is, new CRC32()); ZipInputStream zis = new ZipInputStream(cis)) {
            zipDecompress(zis, destFile);
        }
    }

    private static void zipDecompress(ZipInputStream zis, File destFile) throws IOException {
        while (true) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                break;
            }
            String dir = destFile.getPath() + File.separator + entry.getName();
            File dirFile = new File(dir);
            IOHelper.ensureFolderExists(dirFile);

            if (entry.isDirectory()) {
                dirFile.mkdir();
            }
            else {
                zipDecompressFile(zis, dirFile);
            }
            zis.closeEntry();
        }
    }

    private static void zipDecompressFile(ZipInputStream zis, File destFile) throws IOException {
        int count;
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(destFile.toPath()))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                count = zis.read(buffer, 0, BUFFER_SIZE);
                if (count < 0) {
                    break;
                }
                bos.write(buffer, 0, count);
            }
        }
    }

    public static void zipDecompressAsSingleFile(String sourceFileName, String destFileName) throws IOException {
        try (CheckedInputStream cis = new CheckedInputStream(new FileInputStream(new File(sourceFileName)), new CRC32()); ZipInputStream zis = new ZipInputStream(cis)) {
            //�����п��ܻ�û�� Entry?
            zis.getNextEntry();
            zipDecompressFile(zis, new File(destFileName));
        }
    }

    public static byte[] zipDecompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = null;
        ZipEntry entry = null;
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new CheckedInputStream(new ByteArrayInputStream(data), new CRC32()));
            baos = new ByteArrayOutputStream();
            entry = zis.getNextEntry();
            int count;
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                count = zis.read(buffer, 0, BUFFER_SIZE);
                if (count < 0)
                    break;
                baos.write(buffer, 0, count);
            }
        } finally {
            if (baos != null) {
                baos.close();
            }
            if (entry != null) {
                entry.clone();
            }
            if (zis != null) {
                zis.close();
            }
        }
        return baos.toByteArray();
    }


    public static byte[] zipDecompress(byte[] data, String tempPath, String targetName) throws IOException {
        byte[] result = null;
        //待解压文件名
        String zipPathName = tempPath + File.separator + IdUtil.simpleUUID() + ZIP_SUFFIX;
        //字节数组写入解压文件
        IOHelper.bytesToFile(data, zipPathName);
        //解压文件到目录
        String filePath = tempPath + File.separator + IdUtil.simpleUUID();
        ZipUtil.unzip(zipPathName, filePath);
        //找到目标文件
        List<File> files = FileUtil.loopFiles(filePath);
        for (File file : files) {
            if (StrUtil.containsAny(file.getName(), targetName)) {
                result = FileUtil.readBytes(file);
                break;
            }
        }
        //删除临时文件
        FileUtil.del(zipPathName);
        FileUtil.del(filePath);
        //返回目标文件字节
        return result;
    }


    public static File zipDecompress(byte[] data, String tempPath) throws IOException {
        byte[] result;
        //待解压文件名
        String zipPathName = tempPath + File.separator + IdUtil.simpleUUID() + ZIP_SUFFIX;
        //字节数组写入解压文件
        IOHelper.bytesToFile(data, zipPathName);
        //解压文件到目录
        String filePath = tempPath + File.separator + IdUtil.simpleUUID();
        ZipUtil.unzip(zipPathName, filePath);
        //找到目标文件
        List<File> files = FileUtil.loopFiles(filePath);
        String zipPath = tempPath + File.separator + IdUtil.simpleUUID();
        for (File file : files) {
            result = FileUtil.readBytes(file);
            IOHelper.bytesToFile(result, zipPath + File.separator + file.getName());
        }
        //删除临时文件
        FileUtil.del(zipPathName);
        FileUtil.del(filePath);
        //返回目标文件字节
        return new File(zipPath);
    }
}
