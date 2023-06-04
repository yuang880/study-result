package com.zhangxd.service.impl;

import cn.hutool.core.io.FileUtil;
import com.zhangxd.service.DataExportWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractDataExportWriter implements DataExportWriter {

    /**
     * 默认的切割文件的记录数
     */
    private static final int DEFAULT_SPLIT_THRESHOLD = 100000;
    /**
     * 生成的文件的保存地址
     */
    private final String dirName;

    /**
     * 保存在dirName下的文件前缀
     */
    private final String filePrefix;

    /**
     * 导出文件的后缀名称
     * 要带点的，例如.xlsx
     */
    private final String suffix;

    /**
     * 导出的列的title的map，里面存储了每个列的名称
     */
    protected final Map<String, String> titleMap;

    private int fileSplitThreshold = DEFAULT_SPLIT_THRESHOLD;

    /**
     * 导出的目录工作空间
     */
    private File workspace;

    /**
     * 当前文件的写入数量
     */
    private int currentFileLineNum = 0;

    /**
     * 当前文件编号
     */
    private int currentFileNumber = 0;

    private boolean isInitOver = false;

    public AbstractDataExportWriter(String dirName, String filePrefix, String suffix, Map<String, String> titleMap, int fileSplitThreshold) {
        this.dirName = dirName;
        this.filePrefix = filePrefix;
        this.suffix = suffix;
        this.titleMap = titleMap;
        this.fileSplitThreshold = fileSplitThreshold;
    }

    public AbstractDataExportWriter(String dirName, String filePrefix, String suffix, List<String> columnTitles, int fileSplitThreshold) {
        this(dirName, filePrefix, suffix, ExportUtil.setTitleOrder(columnTitles), fileSplitThreshold);
    }

    public int getFileSplitThreshold() {
        return fileSplitThreshold;
    }

    public void setFileSplitThreshold(int fileSplitThreshold) {
        this.fileSplitThreshold = fileSplitThreshold;
    }

    /**
     * 尝试切换文件
     * <p>
     * 如果无法创建或者无法写入或者是写入失败则通过抛出异常来表示
     */
    protected void tryChangeFile(int willWriteSize) throws IOException {
        if (currentFileLineNum + willWriteSize > fileSplitThreshold) {
            // 关闭上一个旧的文件
            this.closeFile();
            this.createNewOutFile();
        } else if (!isInitOver) {
            // 第一次进入，尚未初始化的情况
            this.createNewOutFile();
            isInitOver = true;
        } else {
            // 写入旧文件
        }
    }

    /**
     * 创建输出文件并进行头部写入
     * @throws IOException
     */
    private void createNewOutFile() throws IOException {
        // 尝试创建目录
        this.tryCreateDir();
        // 创建新文件
        this.createNewFile();
        // 文件编号 + 1
        this.incrementFileNumber();
        // 清空文件行数的计数
        this.updateCurrentFileWriteNum(0);
        // 写入新的文件头
        this.writeHead();
    }

    /**
     * 关闭当前正在操作的文件
     */
    protected abstract void closeFile() throws IOException;

    /**
     * 创建一个新文件,文件的管理由子类完成，父类不需要关心
     *
     */
    protected abstract void createNewFile() throws IOException;

    /**
     * 写文件头
     */
    protected abstract void writeHead() throws IOException;

    /**
     * 更新当前文件的写入数量
     *
     * @param lineNum 新的写入数量
     */
    protected void updateCurrentFileWriteNum(int lineNum) {
        this.currentFileLineNum = lineNum;
    }

    protected String getDirName() {
        return dirName;
    }

    /**
     * 获取当前写入行数
     *
     * @return 当前写入行数
     */
    protected int getCurrentFileLineNum() {
        return currentFileLineNum;
    }


    protected int getCurrentFileNumber() {
        return currentFileNumber;
    }

    protected void incrementFileNumber() {
        this.currentFileNumber = this.currentFileNumber + 1;
    }

    @Override
    public void write(List<Object> data) throws IOException {
        if (Objects.isNull(data) || data.isEmpty()) {
            // 数据为空无需写入
            return;
        }
        int dataSize = data.size();
        // 初始化写入文件或者进行文件切割
        this.tryChangeFile(dataSize);

        for (Object bean : data) {
            // 调用实际的写入方法
            this.write0(bean);
        }
        // 更新写入文件的记录数量
        this.updateCurrentFileWriteNum(this.getCurrentFileLineNum() + dataSize);
    }

    @Override
    public void write(Object data) throws IOException {
        if (Objects.isNull(data)) {
            // 数据为空无需写入
            return;
        }
        // 初始化写入文件或者进行文件切割
        this.tryChangeFile(1);
        // 调用实际的写入方法
        this.write0(data);
        // 更新写入文件的记录数量
        this.updateCurrentFileWriteNum(this.getCurrentFileLineNum() + 1);
    }

    protected String getNewFileName() {
        return filePrefix + this.getCurrentFileNumber() + suffix;
    }

    protected abstract void write0(Object data) throws IOException;

    /**
     * 关闭写入文件
     */
    @Override
    public void close() throws Exception {
        this.closeFile();
    }

    private void tryCreateDir() throws IOException {
        workspace = new File(dirName);
        if (!workspace.exists()) {
            boolean result = workspace.mkdirs();
            if (!result) {
                // 创建文件夹失败
                throw new IOException("create directory " + dirName + " failed!");
            }
        }
    }

    @Override
    public String getExportDirPath() {
        return workspace.getAbsolutePath();
    }

    @Override
    public boolean dispose() {
        return FileUtil.del(workspace);
    }
}
