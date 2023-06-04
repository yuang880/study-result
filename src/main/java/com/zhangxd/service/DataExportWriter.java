package com.zhangxd.service;

import java.io.IOException;
import java.util.List;

public interface DataExportWriter extends AutoCloseable {

    /**
     * 将数据写入到文件中
     */
    void write(List<Object> data) throws IOException;

    /**
     * 将数据写入到文件中
     */
    void write(Object data) throws IOException;

    /**
     * 获取导出目录的文件夹
     */
    String getExportDirPath();

    /**
     * 清除为导出创建的工作空间
     */
    boolean dispose();
}
