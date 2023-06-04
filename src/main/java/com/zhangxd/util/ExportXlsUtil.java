package com.zhangxd.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * description: Xls导出操作工具类 <br>
 */
public class ExportXlsUtil {

    private final static int COLUMN_WIDTH = 256 * 14;

    /**
     * 合并导出下载，不建议大数据量采用该方法
     * @param excelPath  导出路径
     * @param sheetParam 导出数据参数
     */
    public static File exportMergeXls(String excelPath, SheetParam sheetParam) {
        File excelFile = FileUtil.file(excelPath);
        //创建工作薄对象
        try (HSSFWorkbook workbook = new HSSFWorkbook(); FileOutputStream out = new FileOutputStream(excelFile)) {
            //创建工作表对象
            HSSFSheet sheet = workbook.createSheet();
            Map<String, HSSFRow> rowMap = new HashMap<>(sheetParam.getRowCount());

            //第1列特定宽度
            HSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            sheet.setColumnWidth(0, COLUMN_WIDTH);

            //生成表头
            if (CollectionUtil.isNotEmpty(sheetParam.getHeadParamList())) {
                for (SheetHeadParam headParam : sheetParam.getHeadParamList()) {
                    String rowKey = String.valueOf(headParam.getFirstRow());
                    HSSFRow row;

                    if (rowMap.containsKey(rowKey)) {
                        row = rowMap.get(rowKey);
                    } else {
                        //创建工作表的行
                        row = sheet.createRow(headParam.getFirstRow());
                        rowMap.put(rowKey, row);
                    }
                    row.setRowStyle(cellStyle);
                    //单行标题
                    row.createCell(headParam.getFirstCol(), CellType.STRING).setCellValue(headParam.getTitle());
                    //需要合并的才需要处理
                    if (headParam.isMergeSign()) {
                        CellRangeAddress region = new CellRangeAddress(headParam.getFirstRow(), headParam.getLastRow(),
                                headParam.getFirstCol(), headParam.getLastCol());
                        sheet.addMergedRegion(region);
                    }
                }
            }
            //生成内容
            if (CollectionUtil.isNotEmpty(sheetParam.getContentParamList())) {
                for (SheetContentParam sheetContentParam : sheetParam.getContentParamList()) {
                    String rowKey = String.valueOf(sheetContentParam.getRow());
                    HSSFRow row;
                    if (rowMap.containsKey(rowKey)) {
                        row = rowMap.get(rowKey);
                    } else {
                        //创建工作表的行
                        row = sheet.createRow(sheetContentParam.getRow());
                        rowMap.put(rowKey, row);
                    }
                    //单行标题
                    row.createCell(sheetContentParam.getCol(), CellType.STRING).setCellValue(sheetContentParam.getValue());
                }
            }
            workbook.setSheetName(0, sheetParam.getSheetName());
            workbook.write(out);
            return excelFile;
        }  catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
