package com.zhangxd.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.*;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.json.JSONUtil;
import com.zhangxd.common.loginuser.UserContext;
import com.zhangxd.domain.export.ExportParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
public class ExportUtil {

    private static final String URL;

    private static final Integer PORT;

    private static final String USERNAME;

    private static final String PASSWORD;

    public static final String FTP_PATH;

    public static final String LOCAL_STORAGE_PATH;

    static {
        Properties storageProperties = new Properties();
        try (InputStream stream = ExportUtil.class.getResourceAsStream("/storage.properties")) {
            storageProperties.load(stream);
        } catch (Exception ex) {
            throw new RuntimeException("未读取到存储配置文件！");
        }
        URL = storageProperties.getProperty("FTPIP");
        PORT = Integer.parseInt(storageProperties.getProperty("FTPPort"));
        USERNAME = storageProperties.getProperty("FTPUsername");
        PASSWORD = storageProperties.getProperty("FTPPassword");
        FTP_PATH = storageProperties.getProperty("FTPStoragePATH");
        LOCAL_STORAGE_PATH = storageProperties.getProperty("LocalStoragePath");
    }

    /**
     * 用完要记得关闭
     *
     * @return Ftp
     */
    public static Ftp getFtp() {
        return new Ftp(URL, PORT, USERNAME, PASSWORD);
    }

    /**
     * 本地生成临时压缩包上传至ftp做中间站
     * 在进入control层时先获取用户的操作锁，在service层将查询的结果写入excle并上传至ftp，然后将ftp上的路径返回到control层，在control层进行下载。
     *
     * @param exportParam 需要参数sheetName（自定义）
     * @return zipFtpPath FTP路径+重命名名称 两者以逗号隔开 zipFtpPath = ftpFilePath+","+ftpFileName;
     */
    public static void exportDataToFTP(ExportParam exportParam, List datas) {
        if (CollectionUtil.isEmpty(datas)) {
            throw new RuntimeException("可导出的数据为空！");
        }
        if (StrUtil.isEmpty(exportParam.getSheetName())) {
            throw new RuntimeException("表格sheet页名字不能为空！");
        }

        String loginId = UserContext.getUserInfo().getLoginId();
        String userId = UserContext.getUserId();
        String sheetName = exportParam.getSheetName();
        String fileName = getFileName(sheetName);
        LocalDate nowDate = LocalDate.now();
        String year = String.valueOf(nowDate.getYear());
        String month = String.valueOf(nowDate.getMonthValue());
        String day = String.valueOf(nowDate.getDayOfMonth());
        String ftpFilePath = FTP_PATH + StrUtil.SLASH + loginId
                + StrUtil.SLASH + year + StrUtil.SLASH + month + StrUtil.SLASH + day;
        exportParam.setFtpFilePath(ftpFilePath);
        exportParam.setFtpFileName(fileName);
        exportParam.setUserId(userId);
        exportParam.setLoginId(loginId);

        StringBuilder info = new StringBuilder()
                .append("\r\n")
                .append("导出数据至FTP，\r\n")
                .append("当前FTP配置：【").append("URL=").append(URL).append(", PORT=").append(PORT)
                .append(", USERNAME=").append(USERNAME).append(", PASSWORD=").append(PASSWORD).append("】\r\n")
                .append("导出参数【").append(JSONUtil.toJsonStr(exportParam)).append("】\r\n")
                .append("导出数据条数【").append(datas.size()).append("】\r\n");

        //根据条件创建表格
        String excelPath = FileUtil.getTmpDirPath() + File.separator + fileName + ".xlsx";
        File excelFile = createExcel(exportParam, excelPath, datas);
        //压缩文件
        File zipFile = ZipUtil.zip(excelFile);
        //上传FTP
        try {
            uploadToFtp(zipFile, ftpFilePath, info);
        } finally {
            log.info(info.toString());
            FileUtil.del(zipFile);
            FileUtil.del(excelFile);
        }
    }

    public static String getFtpFilePath() {
        String loginId = UserContext.getUserInfo().getLoginId();
        LocalDate nowDate = LocalDate.now();
        String year = String.valueOf(nowDate.getYear());
        String month = String.valueOf(nowDate.getMonthValue());
        String day = String.valueOf(nowDate.getDayOfMonth());
        return FTP_PATH + StrUtil.SLASH + loginId
                + StrUtil.SLASH + year + StrUtil.SLASH + month + StrUtil.SLASH + day;
    }

    /**
     * 上传文件到FTP
     *
     * @param zipFile     压缩文件
     * @param ftpFilePath 路径
     * @param info        错误信息
     */
    public static void uploadToFtp(File zipFile, String ftpFilePath, StringBuilder info) {
        try (Ftp ftp = new Ftp(URL, PORT, USERNAME, PASSWORD)) {
            ftp.mkDirs(ftpFilePath);
            ftp.getClient().enterLocalPassiveMode();
            boolean success = ftp.upload(ftpFilePath, zipFile.getName(), zipFile);
            if (!success) {
                String msg = String.format("FTP上传失败！FTP目录【%s】, 文件名【%s】", ftpFilePath, zipFile.getName());
                throw new RuntimeException(msg);
            }
            info.append("上传FTP成功！\r\n");
        } catch (Exception ex) {
            info.append("上传FTP异常，原因【").append(ex.getMessage()).append("】\r\n");
            throw new RuntimeException("FTP上传异常！", ex);
        }
    }

    /**
     * zip页面下载 在control层使用
     */
    public static void ftpDownload(ExportParam exportParam, HttpServletResponse response) {
        String ftpFilePath = exportParam.getFtpFilePath();
        String ftpFileName = exportParam.getFtpFileName();
        if (StrUtil.isBlank(ftpFileName) || ftpFileName.split("\\.").length < 2) {
            ftpFileName = ftpFileName + ".zip";
        }

        StringBuilder info = new StringBuilder()
                .append("\r\n")
                .append("从FTP下载导出文件，\r\n")
                .append("当前FTP配置：【").append("URL=").append(URL).append(", PORT=").append(PORT)
                .append(", USERNAME=").append(USERNAME).append(", PASSWORD=").append(PASSWORD).append("】\r\n")
                .append("导出参数【").append(JSONUtil.toJsonStr(exportParam)).append("】\r\n");

        try (
                Ftp ftp = new Ftp(URL, PORT, USERNAME, PASSWORD);
                OutputStream outstr = response.getOutputStream()
        ) {
            response.setContentType("application/zip");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment");
            //设置HttpOnly属性
            response.addHeader("Set-Cookie", "uid=112; Path=/; HttpOnly");
            //输出至浏览器
            ftp.getClient().enterLocalPassiveMode();
            ftp.download(ftpFilePath, ftpFileName, outstr);
            info.append("导出成功！\r\n");
        }  catch (Exception ex) {
            info.append("导出异常，原因【").append(ex.getMessage()).append("】\r\n");
            throw new RuntimeException(ex);
        } finally {
            log.info(info.toString());
        }
    }

    /**
     * 根据查询列表类型创建excel表格
     *
     * @param exportParam 需要参数SheetName sheet页名称
     *                    ColumnTitles列表字段名字、顺序以及是否格式化拼接规则为字段id@字段名#是否格式化 如果不需要格式化#后面为空如果需要加入formatter比如组织 orgid@组织#
     *                    downloadType 为列表类型 GRID 和GROUPGRID分别对应普通表格和树形表格
     */
    private static File createExcel(ExportParam exportParam, String excelPath, List datas) {
        //PageView数据转换
        List<Map<String, Object>> pageDatas = new ArrayList<>();
        if (datas.get(0) instanceof Map) {
            for (Object data : datas) {
                pageDatas.add((Map<String, Object>) ObjectUtil.clone(data));
                data = null;
            }
        } else {
            for (Object data : datas) {
                pageDatas.add(BeanUtil.beanToMap(data));
                data = null;
            }
        }

        File excelFile = FileUtil.file(excelPath);
        try (
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                OutputStream excelOut = new FileOutputStream(excelFile)
        ) {
            // 创建工作表
            SXSSFSheet sheet = workbook.createSheet(exportParam.getSheetName());
            // 设置行组合设置,+号显示在行组合上方
            sheet.setRowSumsBelow(false);
            //对title进行排序处理
            Map<String, String> titleMap = setTitleOrder(exportParam.getColumnTitles());
            //创建grid表格内容
            switch (exportParam.getDownloadType()) {
                case GRID:
                    createGridSheetDataByColumn(titleMap, pageDatas, sheet, workbook);
                    break;
                case GROUP_GRID:
                    createGridSheetDataByColumn(titleMap, pageDatas, sheet, workbook);
                    break;
                default:
            }
            sheet.flushRows();
            workbook.write(excelOut);
            workbook.dispose();
            return excelFile;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 将ArrayList转换为有序的map
     */
    public static Map<String, String> setTitleOrder(List<String> titles) {
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        for (String title : titles) {
            String[] keyValue = title.split("@");
            if (keyValue.length != 2) {
                throw new RuntimeException("列表字段数据缺失！");
            }
            linkedHashMap.put(keyValue[0], keyValue[1]);
        }
        return linkedHashMap;
    }

    /**
     * 创建列表表头
     */
    private static int[] createSheetTitle(Map<String, String> title, SXSSFSheet sheet, SXSSFWorkbook wb) {
        int cellIndex = 0;
        int[] columnLengths = new int[title.size()];
        CellStyle xssfCellStyle = leftHeaderCS(wb);
        SXSSFRow row = sheet.createRow(0);
        for (Map.Entry<String, String> entry : title.entrySet()) {
            String cellName = entry.getValue();
            cellName = cellName.split("#")[0];
            SXSSFCell cell = row.createCell(cellIndex);
            cell.setCellValue(cellName);
            cell.setCellStyle(xssfCellStyle);
            columnLengths[cellIndex++] = cellName.getBytes(CharsetUtil.CHARSET_GBK).length + 1;
        }
        return columnLengths;
    }

    /**
     * 创建列表下载内容，并设置列宽
     */
    private static void createGridSheetDataByColumn(Map<String, String> title, List<Map<String, Object>> pageDatas, SXSSFSheet sheet, SXSSFWorkbook wb) {
        // 1.创建标题行
        int[] columnLengths = createSheetTitle(title, sheet, wb);
        // 2.创建内容
        CellStyle xssfCellStyle = leftBgCS(wb);
        for (int rowIndex = 0; rowIndex < pageDatas.size(); rowIndex++) {
            Map<String, Object> data = pageDatas.get(rowIndex);
            //跳过第一行标题
            SXSSFRow row = sheet.createRow(rowIndex + 1);
            int columnNum = 0;
            for (String key : title.keySet()) {
                //数据转换成对应的格式
                String value = transform(data, title, key);
                SXSSFCell cell = row.createCell(columnNum);
                cell.setCellValue(value);
                cell.setCellStyle(xssfCellStyle);
                //统计列宽 取最大值
                columnLengths[columnNum] = Math.max(columnLengths[columnNum], value.getBytes(CharsetUtil.CHARSET_GBK).length + 1);
                columnNum++;
            }
        }
        for (int i = 0; i < columnLengths.length; i++) {
            //excel每格最长255，中文一个顶俩
            int length = columnLengths[i] >= 255 ? 255 : columnLengths[i];
            sheet.setColumnWidth(i, length * 256);
        }
    }

    public static String transform(Map data, Map<String, String> title, String key) {
        String value = null;
        //字段是带.的
        if (key.contains(StrUtil.DOT)) {
            String objKey = key.substring(0, key.indexOf(StrUtil.DOT));
            key = key.substring(key.indexOf(StrUtil.DOT) + 1);
            Object obj = data.get(objKey);
            return transform(BeanUtil.beanToMap(obj), title, key);
        } else if (data.get(key) == null || data.get(key).getClass().isPrimitive()) {
            value = data.get(key) == null ? "" : String.valueOf(data.get(key));
        } else if (data.get(key) instanceof Date) {
            if (title.get(key) != null && title.get(key).indexOf("formatter") > 0) {
                value = DateUtil.format((Date) data.get(key), "yyyy-MM-dd");
            } else {
                value = DateUtil.format((Date) data.get(key), "yyyy-MM-dd HH:mm:ss");
            }
        } else if (data.get(key) instanceof BigDecimal) {
            value = NumberUtil.decimalFormat(",##0.00", ((BigDecimal) data.get(key)).doubleValue());
        } else {
            value = String.valueOf(data.get(key));
        }
        return value;
    }

    /**
     * excel表头样式设置
     */
    private static CellStyle leftHeaderCS(SXSSFWorkbook wb) {
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
    }

    /**
     * excel内容样式设置
     */
    private static CellStyle leftBgCS(SXSSFWorkbook wb) {
        // 左对齐、黑体、背景填充
        short greyColor = IndexedColors.WHITE.getIndex();
        // 默认字体
        Font generalFont = wb.createFont();
        generalFont.setFontName("Arial");
        generalFont.setFontHeightInPoints((short) 10);

        // 左对齐
        CellStyle leftBgCS = wb.createCellStyle();
        leftBgCS.setBorderTop(BorderStyle.THIN);
        leftBgCS.setBorderLeft(BorderStyle.THIN);
        leftBgCS.setBorderBottom(BorderStyle.THIN);
        leftBgCS.setBorderRight(BorderStyle.THIN);
        leftBgCS.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        leftBgCS.setFont(generalFont);
        // 填充背景色
        leftBgCS.setFillForegroundColor(greyColor);
        leftBgCS.setLocked(false);
        return leftBgCS;
    }

    /**
     * 获取excel文件名
     */
    private static String getFileName(String sheetname) {
        StringBuilder name = new StringBuilder();
        name.append(sheetname)
                .append("_")
                .append(DateUtil.format(new Date(), "yyyyMMddHHmmss"))
                .append("_")
                .append(UserContext.getUserInfo().getUserName());
        return name.toString();
    }

    public static String getUrl() {
        return URL;
    }

    public static int getPort() {
        return PORT;
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    public static String getFtpPath() {
        return FTP_PATH;
    }


    /**
     * 本地生成临时压缩包上传至ftp做中间站
     * 在进入control层时先获取用户的操作锁，在service层将查询的结果写入excle并上传至ftp，然后将ftp上的路径返回到control层，在control层进行下载。
     *
     * @param exportParam 需要参数sheetName（自定义）
     * @param sheetParam 导出数据
     * @return zipFtpPath FTP路径+重命名名称 两者以逗号隔开 zipFtpPath = ftpFilePath+","+ftpFileName;
     */
    public static void exportDataToFTP(ExportParam exportParam, SheetParam sheetParam) {
        if (null == sheetParam) {
            throw new RuntimeException("可导出的数据为空！");
        }
        String loginId = UserContext.getUserInfo().getLoginId();
        String userId = UserContext.getUserId();
        String sheetName = exportParam.getSheetName();
        String fileName = getFileName(sheetName);
        LocalDate nowDate = LocalDate.now();
        String year = String.valueOf(nowDate.getYear());
        String month = String.valueOf(nowDate.getMonthValue());
        String day = String.valueOf(nowDate.getDayOfMonth());
        String ftpFilePath = FTP_PATH + StrUtil.SLASH + loginId
                + StrUtil.SLASH + year + StrUtil.SLASH + month + StrUtil.SLASH + day;
        exportParam.setFtpFilePath(ftpFilePath);
        exportParam.setFtpFileName(fileName);
        exportParam.setUserId(userId);
        exportParam.setLoginId(loginId);

        StringBuilder info = new StringBuilder()
                .append("\r\n")
                .append("导出数据至FTP，\r\n")
                .append("当前FTP配置：【").append("URL=").append(URL).append(", PORT=").append(PORT)
                .append(", USERNAME=").append(USERNAME).append(", PASSWORD=").append(PASSWORD).append("】\r\n")
                .append("导出参数【").append(JSONUtil.toJsonStr(exportParam)).append("】\r\n")
                .append("导出数据条数【").append(sheetParam.getContentParamList().size()).append("】\r\n");

        //根据条件创建表格
        String excelPath = FileUtil.getTmpDirPath() + File.separator + fileName + ".xls";
        File excelFile = ExportXlsUtil.exportMergeXls(excelPath, sheetParam);
        //压缩文件
        File zipFile = ZipUtil.zip(excelFile);
        //上传FTP
        try {
            uploadToFtp(zipFile, ftpFilePath, info);
        } finally {
            log.info(info.toString());
            FileUtil.del(zipFile);
            FileUtil.del(excelFile);
        }
    }
}
