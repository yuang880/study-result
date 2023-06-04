package com.zhangxd.util;

import java.io.*;

/**
 * 对于 IO 操作的帮助类
 * @author Administrator
 *
 */
public class IOHelper {
    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 2048;
	/**
	 * 获取当前路径
	 * @return
	 */
	public static String getCurrentDir()
	{
		return IOHelper.class.getClassLoader().getResource(".").getPath();
	}
    /**
     * <p>
     * 文件转换为byte数组
     * </p>
     *
     * @param filePath 文件路径
     * @return
     * @throws IOException
     */
    public static byte[] fileToBytes(String filePath) throws IOException
    {
        byte[] data = new byte[0];
        File file = null;
        FileInputStream in = null;
        ByteArrayOutputStream out  = null;
        try
        {
	        file = new File(filePath);
	        if (file.exists() && file.isFile()) {
	            in = new FileInputStream(file);
	            out = new ByteArrayOutputStream((int)file.length());
	            byte[] cache = new byte[CACHE_SIZE];
	            int nRead = 0;
	            while ((nRead = in.read(cache)) != -1) {
	                out.write(cache, 0, nRead);
	                out.flush();
	            }
	            data = out.toByteArray();
	        }
	        return data;
        }
        finally
        {
        	if(in != null)
        		in.close();
        	if(out != null)
        		out.close();
        }
    }

    /**
     * <p>
     * 二进制数据写文件
     * </p>
     *
     * @param bytes 二进制数据
     * @param filePath 文件生成目录
     * @throws IOException
     */
    public static void bytesToFile(byte[] bytes, String filePath) throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
        	in = new ByteArrayInputStream(bytes);
	        File destFile = new File(filePath);
	        if (!destFile.getParentFile().exists()) {
	            destFile.getParentFile().mkdirs();
	        }
	        destFile.createNewFile();
	        out = new FileOutputStream(destFile);
	        byte[] cache = new byte[CACHE_SIZE];
	        int nRead = 0;
	        while ((nRead = in.read(cache)) != -1) {
	            out.write(cache, 0, nRead);
	            out.flush();
	        }
        }
        finally
        {
        	if(in != null)
        		in.close();
        	if(out != null)
        		out.close();
        }
    }
    /**
     * 确保文件夹存在。不存在则创建
     * @param folder
     */
    public static void ensureFolderExists(File folder)
    {
    	File parentFile = folder.getParentFile();
    	//递归查找上级目录，没有则创建
    	if(! parentFile.exists())
    	{
    		ensureFolderExists(parentFile);
    		parentFile.mkdir();
    	}
    }
}
