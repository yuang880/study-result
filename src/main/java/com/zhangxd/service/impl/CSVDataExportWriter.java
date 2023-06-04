package com.zhangxd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.zhangxd.util.ExportUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author alvinkwok
 * @date 2022/6/22
 */
public class CSVDataExportWriter extends AbstractDataExportWriter {

    private static final String CSV_SUFFIX = ".csv";


    private CSVPrinter csvPrinter;

    private static final String CHARSET = "GBK";

    /**
     * 输出文件
     */
    private File outFile;
    public CSVDataExportWriter(String dirName, String filePrefix, List<String> columnTitles, int fileSplitThreshold) {
        super(dirName, filePrefix, CSV_SUFFIX, columnTitles, fileSplitThreshold);
    }

    @Override
    protected void closeFile() throws IOException {
        this.csvPrinter.close(true);
    }

    @Override
    protected void createNewFile() throws IOException {
        this.outFile = new File(this.getDirName(), this.getNewFileName());
        this.csvPrinter = new CSVPrinter(new OutputStreamWriter(Files.newOutputStream(outFile.toPath()), CHARSET), CSVFormat.EXCEL);
    }


    @Override
    protected void writeHead() throws IOException {
        csvPrinter.printRecord(titleMap.values());
    }

    @Override
    protected void write0(Object data) throws IOException {
        Map<String, Object> map = BeanUtil.beanToMap(data);
        // 写数据
        List<String> dataList = new ArrayList<>();
        for (String key : titleMap.keySet()) {
            String value = ExportUtil.transform(map, titleMap, key);
            // 加 \t 为了让 MicroSoft Excel 打开 csv 的时候不丢失精度。字段过长时不显示 'xxx'
            dataList.add(value + '\t');
        }
        csvPrinter.printRecord(dataList);
    }
}
