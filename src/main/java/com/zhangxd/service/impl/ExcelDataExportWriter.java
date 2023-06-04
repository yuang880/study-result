package com.zhangxd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * 采用POI 3.8版本以后的SXSSFWorkbook解决大数据量导出的时候占用大量内存的问题
 * SXSSFWorkbook 的基本使用方法如下 ：
 * <blockquote><pre>
 *      public static void main(String[] args) throws IOException, InterruptedException {
 * //        Thread.sleep(10000);
 *         SXSSFWorkbook workbook = new SXSSFWorkbook(100);
 *         Sheet sh = workbook.createSheet();
 *         for(int rownum = 0; rownum < 1000; rownum++){
 *             Row row = sh.createRow(rownum);
 *             for(int cellnum = 0; cellnum < 40; cellnum++){
 *                 Cell cell = row.createCell(cellnum);
 *                 String address = new CellReference(cell).formatAsString();
 *                 cell.setCellValue(address);
 *             }
 *         }
 *
 *         OutputStream fis = Files.newOutputStream(Paths.get("F:\\test.xlsx"));
 *         workbook.write(fis);
 *         fis.close();
 *         // dispose of temporary files backing this workbook on disk
 *         workbook.dispose();
 *     }
 * </pre></blockquote>
 * SXSSFWorkbook解决大量内存的方法很简单，在内部设置一个滑动窗口，当超过滑动窗口大小时将行数最低的写入到临时文件中，一个sheet一个临时文件
 * 最后调用SXSSFWorkbook的write方法将文件写入到自定义文件中，合并的时候是将多个sheet的临时文件先合并到一个临时文件再复制到自定义的文件中
 * 如果开启了压缩将会大大减少临时文件的大小
 *
 * @author alvinkwok
 * @date 2022/6/22
 */
public class ExcelDataExportWriter extends AbstractDataExportWriter {
    /**
     * 样式
     */
    private static final String INTEGER_STYLE = "INTEGER_STYLE";
    private static final String NUMBER_STYLE = "NUMBER_STYLE";
    private static final String STRING_STYLE = "STRING_STYLE";
    private static final String HEADER_STYLE = "HEADER_STYLE";
    private static final String EXCEL_SUFFIX = ".xlsx";

    /**
     * 滑动窗口大小
     */
    private int windowSize = 10000;

    /**
     * 输出文件
     */
    private File outFile;

    private SXSSFWorkbook sxssfWorkbook;

    private SXSSFSheet sxssfSheet;

    /**
     * 列宽
     */
    private int[] columnLengths;

    /**
     * 缓存styleMap，避免进行大量创建
     */
    private Map<String, CellStyle> cellStyleMap;


    public ExcelDataExportWriter(String dirName, String filePrefix, List<String> columnTitles, int fileSplitThreshold) {
        super(dirName, filePrefix, EXCEL_SUFFIX, columnTitles, fileSplitThreshold);
    }

    private void initCellStyle() {
        this.cellStyleMap = new HashMap<>();
    }

    private void initColumnLength() {
        int cellIndex = 0;
        this.columnLengths = new int[titleMap.size()];
        for (Map.Entry<String, String> entry : titleMap.entrySet()) {
            String cellName = entry.getValue();
            cellName = cellName.split("#")[0];
            this.columnLengths[cellIndex++] = cellName.getBytes(CharsetUtil.CHARSET_GBK).length + 1;
        }
    }

    @Override
    protected void closeFile() throws IOException {
        // 关闭之前尝试再调整下列宽
        this.updateColumnLength();

        try (FileOutputStream os = new FileOutputStream(outFile)) {
            // 将已写入的数据写到目标文件中
            sxssfWorkbook.write(os);
            // 关闭写入流
            os.close();
            // 关闭工作表
            sxssfWorkbook.close();
            // 丢弃临时文件释放磁盘空间
            sxssfWorkbook.dispose();
        }
        // 不做catch，往上抛出，只利用try-with-resources关闭文件资源
    }

    @Override
    protected void createNewFile() throws IOException {
        outFile = new File(this.getDirName(), this.getNewFileName());
        // 创建poi 的workbook
        sxssfWorkbook = new SXSSFWorkbook(windowSize);
        sxssfSheet = sxssfWorkbook.createSheet();
        // 清空格式，新文件要重新创建，否则Excel打开的时候会提示内容有问题，需要修复。
        // https://blog.csdn.net/qq_34972627/article/details/124102427
        this.initCellStyle();
        // 重新计算列长度
        this.initColumnLength();
        // 设置好列宽
        this.updateColumnLength();
    }

    @Override
    protected void writeHead() throws IOException {
        int cellIndex = 0;
        // 获取excel标题列的单元格格式
        CellStyle xssfCellStyle = this.getHeaderCellStyle(sxssfWorkbook);
        // 创建第0行作为标题行
        SXSSFRow row = sxssfSheet.createRow(0);
        // 遍历titleMap写入标题行
        for (Map.Entry<String, String> entry : titleMap.entrySet()) {
            String cellName = entry.getValue();
            cellName = cellName.split("#")[0];
            SXSSFCell cell = row.createCell(cellIndex++);
            cell.setCellValue(cellName);
            cell.setCellStyle(xssfCellStyle);
        }
        // 更新当前的文件写入行数
        this.updateCurrentFileWriteNum(1);
    }

    /**
     * excel表头样式设置
     */
    private CellStyle getHeaderCellStyle(SXSSFWorkbook wb) {
        return this.cellStyleMap.computeIfAbsent(HEADER_STYLE, (k) -> {
            short greyColor = IndexedColors.LIGHT_BLUE.getIndex();
            // 左对齐、黑体、背景填充
            CellStyle leftHeaderCS = wb.createCellStyle();
            // 默认字体
            Font generalFont = wb.createFont();
            generalFont.setFontName("Arial");
            generalFont.setFontHeightInPoints((short) 10);
            // 粗体
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            boldFont.setFontName("Arial");
            boldFont.setFontHeightInPoints((short) 10);
            leftHeaderCS.setFont(boldFont);

            leftHeaderCS.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // 填充背景色
            leftHeaderCS.setFillForegroundColor(greyColor);
            leftHeaderCS.setLocked(true);
            return leftHeaderCS;
        });
    }

    /**
     * 获取整数单元格格式
     */
    private CellStyle getIntegerStyle() {
        return this.cellStyleMap.computeIfAbsent(INTEGER_STYLE, (k) -> {
            CellStyle commonStyle = this.newCommonStyle();
            DataFormat dataFormat = sxssfWorkbook.createDataFormat();
            commonStyle.setDataFormat(dataFormat.getFormat("#,##0"));
            return commonStyle;
        });
    }

    /**
     * excel内容样式设置
     */
    private CellStyle getNumberStyle() {
        return this.cellStyleMap.computeIfAbsent(NUMBER_STYLE, (k) -> {
            CellStyle commonStyle = this.newCommonStyle();
            // 设置金额格式
            DataFormat dataFormat = sxssfWorkbook.createDataFormat();
            commonStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
            return commonStyle;
        });

    }

    private CellStyle getStringStyle() {
        return this.cellStyleMap.computeIfAbsent(STRING_STYLE, (k) -> this.newCommonStyle());
    }

    private CellStyle newCommonStyle() {
        // 左对齐、黑体、背景填充
        short greyColor = IndexedColors.WHITE.getIndex();
        // 默认字体
        Font generalFont = sxssfWorkbook.createFont();
        generalFont.setFontName("Arial");
        generalFont.setFontHeightInPoints((short) 10);

        // 左对齐
        CellStyle tempCellStyle = sxssfWorkbook.createCellStyle();
        tempCellStyle.setBorderTop(BorderStyle.THIN);
        tempCellStyle.setBorderLeft(BorderStyle.THIN);
        tempCellStyle.setBorderBottom(BorderStyle.THIN);
        tempCellStyle.setBorderRight(BorderStyle.THIN);
        tempCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tempCellStyle.setFont(generalFont);
        // 填充背景色
        tempCellStyle.setFillForegroundColor(greyColor);
        tempCellStyle.setLocked(false);
        return tempCellStyle;
    }


    @Override
    protected void write0(Object data) {
        int lineNum = this.getCurrentFileLineNum();
        SXSSFRow row = sxssfSheet.createRow(lineNum);
        // 写入明细数据
        Map<String, Object> beanMap = BeanUtil.beanToMap(data);
        int columnNum = 0;
        for (String key : titleMap.keySet()) {
            // 创建单元格
            SXSSFCell cell = row.createCell(columnNum);
            Object obj = beanMap.get(key);
            this.writeValueWithFormat(obj, key, cell, columnNum);
            //统计列宽 取最大值

            columnNum++;
        }
    }

    /**
     * 判断数据类型并按照一定的格式写入excel
     * // short,int 当int
     * // float,double,bigDecimal 当double
     * // 日期转换后当字符串写入
     * // 字符串金额 转化后当double写入
     * // 其余当做字符串写入
     *
     * double对应的格式为 #，##0.00
     * int 对应的格式为 #,##0
     * 字符串属于常规格式，无需设置
     * @param obj 写入值
     * @param key 对应的key
     * @param cell 单元格
     * @param columnNum  所在列序号
     */
    private void writeValueWithFormat(Object obj, String key, Cell cell, int columnNum) {
        //数据转换成对应的格式
        String value;
        // 数据为空无法判断后续类型，原本是可以不做写入的，但是在单元格样式上不写入就会存在一个空框
        // 看起来并不好看，所以数据为空写入空串
        if (obj == null) {
            value = StrUtil.EMPTY;
            this.writeString(value, cell);
            return;
        }
        // 内容写入
        if (obj instanceof Short) {
            Short sv = (Short) obj;
            this.writeInteger(sv.intValue(), cell);
        } else if (obj instanceof Integer) {
            Integer iv = (Integer) obj;
            this.writeInteger(iv, cell);
        } else if (obj instanceof Float) {
            Float fv = (Float) obj;
            this.writeNumber(fv.doubleValue(), cell);
        } else if (obj instanceof Double) {
            Double dv = (Double) obj;
            this.writeNumber(dv, cell);
        } else if (obj instanceof Date) {
            // 日期格式化
            if (titleMap.get(key) != null && titleMap.get(key).indexOf("formatter") > 0) {
                value = DateUtil.format((Date) obj, "yyyy-MM-dd");
            } else {
                value = DateUtil.format((Date) obj, "yyyy-MM-dd HH:mm:ss");
            }
            this.writeString(value, cell);
        } else if (obj instanceof BigDecimal) {
            // 数值格式化
            BigDecimal v = (BigDecimal) obj;
            this.writeNumber(v.doubleValue(), cell);
        } else if (obj instanceof String && StrUtil.isNotBlank(key) && key.toLowerCase().contains("amount")) {
            // 该判断是对字符串金额的特殊处理，只要包含了amount字段就认为是金额，否则其余都当作字符串处理
            value = (String) obj;
            if (StrUtil.isNotBlank(value)) {
                // 按照指定的数值格式进行转换
                Number number = NumberUtil.parseNumber(value);
                this.writeNumber(number.doubleValue(), cell);
            } else {
                // 为空不设置值
                this.writeString(StrUtil.EMPTY, cell);
            }
        } else {
            // 其余基本类型和字符串类型
            value = String.valueOf(obj);
            this.writeString(value, cell);
            columnLengths[columnNum] = Math.max(columnLengths[columnNum], value.getBytes(CharsetUtil.CHARSET_GBK).length + 1);
        }
    }

    /**
     * 写入整数
     *
     * @param obj  写入值
     * @param cell 单元格
     */
    private void writeInteger(int obj, Cell cell) {
        //设置单元格格式
        cell.setCellStyle(this.getIntegerStyle());
        cell.setCellValue(obj);
    }

    /**
     * 写入数值
     *
     * @param obj  写入值
     * @param cell 单元格
     */
    private void writeNumber(double obj, Cell cell) {
        //设置单元格格式
        cell.setCellStyle(this.getNumberStyle());
        cell.setCellValue(obj);
    }

    /**
     * 写入字符串格式
     *
     * @param value 写入值
     * @param cell  单元格
     */
    private void writeString(String value, Cell cell) {
        //设置单元格格式
        cell.setCellStyle(this.getStringStyle());
        cell.setCellValue(value);
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    private void updateColumnLength() {
        // 设置好列宽
        for (int i = 0; i < columnLengths.length; i++) {
            //excel每格最长255，中文一个顶俩
            int length = Math.min(columnLengths[i], 255);
            sxssfSheet.setColumnWidth(i, length * 256);
        }
    }
}
