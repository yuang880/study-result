package com.zhangxd.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import com.rabbitmq.client.Channel;
import com.zhangxd.common.PageView;
import com.zhangxd.common.ResultContext;
import com.zhangxd.common.loginuser.UserContext;
import com.zhangxd.common.pagehelper.PageHelperWrapper;
import com.zhangxd.common.translator.DataTranslator;
import com.zhangxd.domain.BaseCrossDBOperationDTO;
import com.zhangxd.domain.BaseQueryParams;
import com.zhangxd.domain.SelectAllIncludeParam;
import com.zhangxd.domain.export.*;
import com.zhangxd.enums.ENCachePrefix;
import com.zhangxd.enums.ENDataExportStatus;
import com.zhangxd.enums.ENExportDeal;
import com.zhangxd.manager.DataExportRecordManager;
import com.zhangxd.manager.systemproperties.SystemPropertiesManager;
import com.zhangxd.mq.Producer;
import com.zhangxd.service.DataExportService;
import com.zhangxd.service.DataExportWriter;
import com.zhangxd.service.ExportDeal;
import com.zhangxd.service.ExportDealFactory;
import com.zhangxd.util.ExportUtil;
import com.zhangxd.util.ZipHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Chengwy
 * @version 1.0  2022/4/7
 * @description
 */

@Service
@Slf4j
public class DataExportServiceImpl implements DataExportService, ExportDeal<DataExportRecordDTO, DataExportRecordParam> {
    @Autowired
    private ExportDealFactory exportDealFactory;

    @Autowired
    private DataExportRecordManager dataExportRecordManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private Producer producer;

    @Autowired
    private SystemPropertiesManager systemPropertiesManager;

    @Override
    public boolean asyncExportTaskCommitToMq(DataExportTask task) {
        try {
            log.info("导出任务[{}]开始向路由键[{}]推送消息：[{}]", task.getRecordId(), "key_dataexport", JSON.toJSONString(task));
            producer.sendMessage("key_dataexport", JSON.toJSONString(task));
            log.info("导出任务[{}]添加到MQ队列成功！", task.getRecordId());
        } catch (Exception e) {
            log.error("导出任务[{}]添加同步任务到MQ失败！", task.getRecordId());
            return false;
        }
        return true;
    }

    @Override
    public <Q extends BaseQueryParams & ExportExtraParam> ResultContext<Void> commonExport(Q queryParam, ExportParam exportParam, ENExportDeal exportDealType) {
        log.info("页面[{}]接收到导出任务开始！", exportDealType.getLabel());
        // 1.组装导出参数
        DataExportRecordDTO dataExportRecordDTO = initRecord(queryParam, exportParam, exportDealType);
        // 2.并插入数据库
        int insert = dataExportRecordManager.insert(dataExportRecordDTO);
        if (insert != 1) {
            throw new RuntimeException("导出记录插入数据库异常");
        }
        log.info("页面[{}]导出任务[{}]开始生成开始...", ENExportDeal.getLabelByValue(dataExportRecordDTO.getDataSource()), dataExportRecordDTO.getUrid());
        // 3.组装任务参数
        DataExportTask dataExportTask = initTask(queryParam, dataExportRecordDTO);
        // 4.丢进MQ
        boolean success = asyncExportTaskCommitToMq(dataExportTask);
        if (!success) {
            dataExportRecordDTO.setExceptionMessage("加入队列失败");
            this.updateStateWithPreState(dataExportRecordDTO, ENDataExportStatus.QUEUE.getValue(), ENDataExportStatus.EXCEPTION.getValue());
        }
        return success ? ResultContext.buildSuccess("异步请求成功", null) : ResultContext.businessFail("异步请求失败", null);
    }

    private <Q extends BaseQueryParams & ExportExtraParam> DataExportRecordDTO initRecord(Q queryParam, ExportParam exportPara, ENExportDeal exportDealType) {
        // 1. 获取exportDeal 并获取待导出数量
        ExportDeal<?, Q> exportDeal = (ExportDeal<?, Q>) exportDealFactory.getExportDeal(exportDealType);
        long exportCount = exportDeal.countForExport(queryParam);
        if (exportCount == 0) {
            log.error("页面[{}]可导出的数据为空！", exportDealType.getLabel());
            throw new RuntimeException("可导出的数据为空！");
        }

        // 2. 组装导出记录
        String recordId = IdUtil.simpleUUID();
        DataExportRecordDTO dataExportRecordDTO = new DataExportRecordDTO();
        dataExportRecordDTO.setUrid(recordId);
        dataExportRecordDTO.setCreator(UserContext.getUserId());
        dataExportRecordDTO.setCreateDate(new Date());
        dataExportRecordDTO.setUpdateDate(new Date());
        dataExportRecordDTO.setStatus(ENDataExportStatus.QUEUE.getValue());
        String ftpFilePath = ExportUtil.getFtpFilePath();
        dataExportRecordDTO.setStoragePath(ftpFilePath + StrUtil.SLASH + recordId + ".zip");
        dataExportRecordDTO.setProgress(0.0);
        dataExportRecordDTO.setDataSource(exportDealType.getValue());
        dataExportRecordDTO.setExportNum(exportCount);
        dataExportRecordDTO.setExportParam(JSON.toJSONString(exportPara));
        return dataExportRecordDTO;
    }

    private <Q extends BaseQueryParams> DataExportTask initTask(Q queryParam, DataExportRecordDTO dataExportRecordDTO) {
        DataExportTask dataExportTask = new DataExportTask();
        dataExportTask.setExportDeal(dataExportRecordDTO.getDataSource());
        dataExportTask.setRecordId(dataExportRecordDTO.getUrid());
        dataExportTask.setQueryParamJson(JSON.toJSONString(queryParam));
        return dataExportTask;
    }

    @Override
    public PageView<DataExportRecordDTO> listRecord(DataExportRecordParam dataExportRecordParam) {
        PageInfo<DataExportRecordDTO> pageInfo = dataExportRecordManager.pageQuery(dataExportRecordParam);
        PageView<DataExportRecordDTO> pageView = new PageView<>();
        translate(pageInfo.getList());
        pageView.setRows(pageInfo.getList());
        pageView.setTotal(pageInfo.getTotal());
        return pageView;
    }

    @Override
    public ResultContext<DataExportRecordDTO> getDetail(String recordId) {
        DataExportRecordDTO dto = dataExportRecordManager.getById(recordId);
        translateOne(dto);
        if (ObjectUtil.isNull(dto)) {
            return ResultContext.businessFail("记录不存在", null);
        }
        return ResultContext.buildSuccess("查询成功", dto);
    }

    private void translate(List<DataExportRecordDTO> dataExportRecordDTOList) {
        if (CollectionUtil.isNotEmpty(dataExportRecordDTOList)) {
            dataExportRecordDTOList.parallelStream().forEach(this::translateOne);
        }
    }

    private void translateOne(DataExportRecordDTO dataExportRecordDTO) {
        UserDTO creator = userManager.getById(dataExportRecordDTO.getCreator());
        if (creator != null) {
            dataExportRecordDTO.setCreatorShow(creator.getName());
        }
        if (Objects.nonNull(dataExportRecordDTO.getProgress())) {
            dataExportRecordDTO.setProgressShow(dataExportRecordDTO.getProgress() + "%");
        }
        dataExportRecordDTO.setDataSourceShow(ENExportDeal.getLabelByValue(dataExportRecordDTO.getDataSource()));
        dataExportRecordDTO.setStatusShow(ENDataExportStatus.getLabelByValue(dataExportRecordDTO.getStatus()));
    }

    @Override
    public <R> void dealMessage(DataExportTask dataExportTask, Message message, Channel channel) {
        // 消息ack
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("任务ID[{}]ACK异常！", dataExportTask.getRecordId(), e);
        }

        // 执行导出任务
        this.doExportTask(dataExportTask);
    }

    /**
     * 执行导出任务
     *
     * @param dataExportTask 导出任务
     * @param <R>            查出的单挑数据
     */
    private <R> void doExportTask(DataExportTask dataExportTask) {
        // 1.获取导出记录
        DataExportRecordDTO dataExportRecordDTO = dataExportRecordManager.getById(dataExportTask.getRecordId());
        if (ObjectUtil.isNull(dataExportRecordDTO)) {
            log.error("任务ID[{}]找不到对应的导出记录，执行失败！", dataExportTask.getRecordId());
            return;
        }
        log.info("任务ID[{}]执行导出任务开始", dataExportTask.getRecordId());

        File dir = null;
        File zipFile = null;
        DataExportWriter writer = null;
        try {
            // 更新导出记录状态为处理中
            this.updateStateWithPreState(dataExportRecordDTO, ENDataExportStatus.QUEUE.getValue(), ENDataExportStatus.PROCESS.getValue());
            // 初始化相关对象
            long totalCount = dataExportRecordDTO.getExportNum();
            AtomicLong pointCount = new AtomicLong();
            ExportParam exportParam = JSON.parseObject(dataExportRecordDTO.getExportParam(), ExportParam.class);
            AtomicLong nextNeedUpdateCount = new AtomicLong((long) Math.ceil(totalCount * 0.1));
            String dirName = ExportUtil.LOCAL_STORAGE_PATH + File.separator + dataExportRecordDTO.getUrid();
            dir = new File(dirName);
            // 创建导出数据写入器
            // 内部会尝试创建文件目录
            writer = this.getDataExportWriter(dirName,
                    exportParam.getExportFormat(), dataExportRecordDTO.getUrid(),
                    exportParam.getFtpFileName(), exportParam.getColumnTitles());


            // 3.分商户查询并写数据
            DataExportWriter finalWriter = writer;
            // 改转换是安全的不做检查
            @SuppressWarnings("unchecked")
            ExportDeal<R, ?> exportDeal = (ExportDeal<R, ?>) exportDealFactory.getExportDeal(dataExportTask.getExportDeal());
            exportDeal.dataExport(dataExportTask.getQueryParamJson(), (R resultDto) ->
                    this.dealOneDto(dataExportRecordDTO, resultDto, pointCount, finalWriter, nextNeedUpdateCount));
            // 忽略实际导出数量与预估数量不符的情况，因为操作数据库时有可能有额外的数据进出。
            // 关闭文件流
            try {
                writer.close();
            } catch (Exception e) {
                log.error("关闭文件流异常！请检查是否采用了流式导出但没有设置mapper返回值为void", e);
                throw new RuntimeException("关闭文件流异常，可能原因为导出的记录为空", e);
            }

            // 4.压缩
            zipFile = this.compressFile(dataExportRecordDTO, dir);
            // 5.上传至FTP
            this.uploadToFtp(zipFile, dataExportRecordDTO);
        } catch (Exception e) {
            log.error("任务ID[{}]导出任务执行异常！", dataExportRecordDTO.getUrid(), e);
            this.dealException(dataExportRecordDTO, dir, zipFile, StrUtil.isNotBlank(e.getMessage()) ? e.getMessage() : "导出任务执行异常");

            // 关闭writer
            if (Objects.nonNull(writer)) {
                try {
                    writer.close();
                } catch (Exception ioe) {
                    this.dealException(dataExportRecordDTO, dir, zipFile, StrUtil.isNotBlank(ioe.getMessage()) ? ioe.getMessage() : "导出任务执行异常");
                }
            }
        } finally {
            // 删除原始文件
            if (Objects.nonNull(dir)) {
                boolean b = FileUtil.del(dir);
                log.info("任务ID[{}]删除原始文件，结果[{}]", dataExportRecordDTO.getUrid(), b);
            }
        }
    }

    /**
     * 压缩目录
     *
     * @param dataExportRecordDTO 导出任务
     * @param dir                 需要压缩的目录
     * @return 返回压缩后的文件
     * @throws IOException 如果压缩过程出现异常则向上抛出
     */
    private File compressFile(DataExportRecordDTO dataExportRecordDTO, File dir) throws IOException {
        log.info("任务ID[{}]文件生成完毕，开始压缩", dataExportRecordDTO.getUrid());
        File zipFile = new File(dir.getPath() + ".zip");
        ZipHelper.zipCompress(dir, zipFile);
        return zipFile;
    }

    @Override
    public <Q extends BaseQueryParams & ExportExtraParam> boolean checkAndDealDatabaseFields(ExportParam exportParam, Q queryParam, List<String> mustNeedList) {
        List<String> dataBaseFieldList = exportParam.getDataBaseFieldList();
        List<String> columnTitles = exportParam.getColumnTitles();
        if (CollectionUtil.isEmpty(dataBaseFieldList) || CollectionUtil.isEmpty(columnTitles)) {
            log.error("数据库字段或导出参数为空！");
            return false;
        }

        // 添加必填字段并去重
        dataBaseFieldList.addAll(mustNeedList);
        HashSet<String> noRepeatDataBaseFieldSet = new HashSet<>(dataBaseFieldList);

        StringJoiner joiner = new StringJoiner(StrUtil.COMMA);
        noRepeatDataBaseFieldSet.forEach(joiner::add);

        queryParam.setDataBaseFields(joiner.toString());
        return true;
    }

    /**
     * 处理一条数据库记录
     *
     * @param dataExportRecordDTO 导出记录DTO
     * @param resultDto           查出的数据库记录DTO
     * @param pointCount          处理到的记录数
     * @param nextNeedUpdateCount 下一次更新计数
     * @param <R>                 查出的数据库记录类型
     * @return 空
     */
    private <R> void dealOneDto(DataExportRecordDTO dataExportRecordDTO, R resultDto, AtomicLong pointCount, DataExportWriter writer, AtomicLong nextNeedUpdateCount) {
        // 写数据
        try {
            writer.write(resultDto);
        } catch (IOException e) {
            log.error("任务ID[{}], 写内容失败!", dataExportRecordDTO.getUrid(), e);
            throw new RuntimeException(e);
        }
        pointCount.getAndIncrement();
        // 计算是否进度向前动了10%以上，动了就更新数据库
        this.checkIfMoreThanTenPercent(pointCount.get(), nextNeedUpdateCount, dataExportRecordDTO);
    }

    private void uploadToFtp(File zipFile, DataExportRecordDTO dataExportRecordDTO) {
        log.info("任务ID[{}]文件压缩完成，开始上传至FTP", dataExportRecordDTO.getUrid());
        // 状态更新为上传中
        this.updateStateWithPreState(dataExportRecordDTO, dataExportRecordDTO.getStatus(), ENDataExportStatus.UPLOAD.getValue());
        // 这里失败是会向上抛出异常的
        ExportUtil.uploadToFtp(zipFile, dataExportRecordDTO.getStoragePath(), new StringBuilder());
        log.info("任务ID[{}]上传zip文件到FTP成功", dataExportRecordDTO.getUrid());
        // 状态更新为导出完成
        dataExportRecordDTO.setProgress(100.0);
        this.updateStateWithPreState(dataExportRecordDTO, dataExportRecordDTO.getStatus(), ENDataExportStatus.SUCCESS.getValue());
    }

    private void checkIfMoreThanTenPercent(long pointCount, AtomicLong nextNeedUpdateCount, DataExportRecordDTO dataExportRecordDTO) {
        if (pointCount != nextNeedUpdateCount.get()) {
            return;
        }
        // 到达下一次
        double process = (double) nextNeedUpdateCount.get() / dataExportRecordDTO.getExportNum() * 100;
        // 进度到达 100 时，将进度改为 99
        if (nextNeedUpdateCount.get() == dataExportRecordDTO.getExportNum()) {
            process = 99;
        }
        // 保留两位小数
        process = Double.parseDouble(String.format("%.02f", process));
        dataExportRecordDTO.setProgress(process);

        log.info("任务ID[{}]更新进度为[{}]", dataExportRecordDTO.getUrid(), process);
        this.updateStateWithPreState(dataExportRecordDTO, dataExportRecordDTO.getStatus(), dataExportRecordDTO.getStatus());

        // 设置下一次的更新次数
        double nextProcess = ((int) Math.floor(process / 10) + 1) * 10;
        long nextUpdate = (long) Math.ceil(dataExportRecordDTO.getExportNum() * nextProcess / 100);
        nextNeedUpdateCount.set(nextUpdate);
    }

    /**
     * 异常时将状态改为 5 导出异常，并删除记录
     *
     * @param dataExportRecordDTO 导出记录dto
     */
    private void dealException(DataExportRecordDTO dataExportRecordDTO, File dir, File zipFile, String errMsg) {
        dataExportRecordDTO.setStatus(ENDataExportStatus.EXCEPTION.getValue());
        if (errMsg.length() > 256) {
            errMsg = MessageConverter.cutString(errMsg.getBytes(Charset.forName(BankfcConstants.CHARSET_GBK)), 256);
        }
        dataExportRecordDTO.setExceptionMessage(errMsg);
        if (dataExportRecordManager.update(dataExportRecordDTO) != 1) {
            log.error("任务ID[{}]导出异常时将记录更新为异常失败！", dataExportRecordDTO.getUrid());
        }
        // 删除创建的工作空间下的文件及目录
        boolean deleteFlag = false;
        if (Objects.nonNull(dir)) {
            deleteFlag = FileUtil.del(dir);
        }
        // 删除压缩文件
        if (Objects.nonNull(zipFile)) {
            if (FileUtil.exist(zipFile)) {
                deleteFlag = zipFile.delete();
            }
            log.info("任务ID[{}]删除缓存文件[{}]", dataExportRecordDTO.getUrid(), deleteFlag ? "成功" : "失败");
        }
    }

    /**
     * 待前置状态的更新接口
     *
     * @param dataExportRecordDTO 导出记录
     * @param preState            前置状态
     * @param state               更新后的状态
     */
    private void updateStateWithPreState(DataExportRecordDTO dataExportRecordDTO, String preState, String state) {
        dataExportRecordDTO.setStatus(state);
        DatabaseRoute.doWithDatabaseKey(DatabaseRoute.DEFAULT_DATABASE_KEY, () -> {
            if (dataExportRecordManager.updateWithPreState(dataExportRecordDTO, preState) != 1) {
                log.error("任务ID[{}]将记录状态更新为[{}]失败！", dataExportRecordDTO.getUrid(), state);
            }
            return null;
        });
    }

    /**
     * 获取单个文件最大行数
     *
     * @return 最大行数
     */
    private int getMaxLengthPerFile() {
        SystemPropertiesCacheDTO exportSplitPerFile = systemPropertiesManager.getCache(ENCachePrefix.T_SYSTEM_PROPERTIES, ENSysPropertiesKey.EXPORT_SPLIT_PER_FILE.getValue());
        if (ObjectUtil.isNotNull(exportSplitPerFile)) {
            return Integer.parseInt(exportSplitPerFile.getValue());
        }
        // 默认给1百万， excel无法打开超过106万行数据的表格
        return 1000000;
    }

    @Override
    public ResultContext<Integer> countInvalidRecord(DataExportRecordParam dataExportRecordParam) {
        ResultContext<Integer> resultContext = ResultContext.success("查询成功");
        resultContext.setData(dataExportRecordManager.countInvalidRecord(dataExportRecordParam));
        return resultContext;
    }

    @Override
    public ResultContext<List<DataExportRecordDTO>> listInvalidRecord(DataExportRecordParam dataExportRecordParam) {
        PageHelperWrapper.startPage(dataExportRecordParam.getPageNum(), dataExportRecordParam.getPageSize(), false);
        List<DataExportRecordDTO> result = dataExportRecordManager.listInvalidRecord(dataExportRecordParam);
        return ResultContext.buildSuccess("查询成功", result);
    }

    @Override
    public ResultContext<Void> deleteExpireCacheFile(List<DataExportRecordDTO> expireRecordList) {
        if (CollectionUtil.isEmpty(expireRecordList)) {
            log.error("[数据导出]缓存文件删除接口调用时参数为空");
            return ResultContext.businessFail("请求参数为空", null);
        } else {
            // 存储成功删除的记录数
            int deleteCount = 0;
            // todo 缓存文件的存储路径
            String storagePath = ExportUtil.LOCAL_STORAGE_PATH;
            for (DataExportRecordDTO record : expireRecordList) {
                String ftpStoragePath = record.getStoragePath();
                if (StrUtil.isBlank(ftpStoragePath)) {
                    log.warn("[数据导出]缓存文件没有存储路径但是状态是操作成功");
                } else {
                    // 截取文件名
                    // 可能存储的路径为/xxx/xxxx.zip或直接就是一个xxxx.zip，此时直接index是找不到的，返回-1，需要整个文件直接读取
                    int index = ftpStoragePath.lastIndexOf("/");
                    String filename = null;
                    if (index < 0) {
                        filename = ftpStoragePath;
                    } else {
                        filename = ftpStoragePath.substring(index + 1);
                    }
                    File file = new File(storagePath, filename);
                    if (file.delete()) {
                        log.info("[数据导出]缓存文件[{}]删除成功,记录URID[{}]", file.getAbsoluteFile(), record.getUrid());
                    } else {
                        log.warn("[数据导出]缓存文件[{}]删除失败,记录URID[{}]", file.getAbsoluteFile(), record.getUrid());
                        deleteCount++;
                    }
                }
            }
            return ResultContext.buildSuccessNum("删除成功", deleteCount);
        }
    }

    @Override
    public ResultContext<List<DataExportRecordDTO>> deleteExpireFTPFile(List<DataExportRecordDTO> expireRecordList) {
        if (CollectionUtil.isEmpty(expireRecordList)) {
            log.error("[数据导出]缓存文件删除接口调用时参数为空");
            return ResultContext.buildSuccess("请求参数为空", Collections.emptyList());
        } else {
            List<DataExportRecordDTO> deleteSuccessList = new LinkedList<>();
            // 连接FTP
            FileToolClientAdapter client = null;
            try {
                client = new FTPToolClientAdapter();
                boolean isConnect = client.connect(ExportUtil.getUrl(), ExportUtil.getPort(),
                        ExportUtil.getUsername(), ExportUtil.getPassword());
                if (!isConnect) {
                    log.error("[数据导出]缓存文件删除时连接FTP失败");
                    return ResultContext.businessFail("连接FTP失败", null);
                } else {
                    // 迭代删除文件
                    for (DataExportRecordDTO record : expireRecordList) {
                        try {
                            String remote = record.getStoragePath().replace("\\", "/");
                            int index = remote.lastIndexOf("/");
                            if (index == -1) {
                                log.error("[数据导出]记录URID[{}]远程文件名有误[{}]", record.getUrid(), record.getStoragePath());
                                continue;
                            }
                            String fileName = remote.substring(index + 1);
                            if (client.deleteFile(record.getStoragePath() + "/" + fileName)) {
                                deleteSuccessList.add(record);
                            } else {
                                log.error("[数据导出]删除存储文件[{}],记录URID[{}]失败", record.getStoragePath(), record.getUrid());
                            }
                        } catch (Exception e) {
                            log.error("[数据导出]删除存储文件[{}]出现异常,记录URID[{}]", record.getStoragePath(), record.getUrid(), e);
                        }
                    }
                }
                // 连接FTP并删除文件
                return ResultContext.buildSuccess("操作成功", deleteSuccessList);
            } catch (Exception e) {
                // 连接FTP并删除文件
                return ResultContext.businessFail("操作失败", null);
            } finally {
                if (Objects.nonNull(client)) {
                    client.close();
                }
            }
        }
    }

    @Override
    public ResultContext<Void> expireDataExportRecord(List<DataExportRecordDTO> expireRecordList) {
        if (CollectionUtil.isEmpty(expireRecordList)) {
            log.error("[数据导出]缓存文件删除接口调用时参数为空");
            return ResultContext.businessFail("请求参数为空", null);
        } else {
            for (DataExportRecordDTO recordDTO : expireRecordList) {
                recordDTO.setStatus(ENDataExportStatus.EXPIRE.getValue());
                int result = dataExportRecordManager.updateWithPreState(recordDTO, ENDataExportStatus.SUCCESS.getValue());
                if (result != 1) {
                    log.error("[数据导出]将记录[{}]更新为失效时异常,前置状态[{}]", recordDTO.getUrid(), ENDataExportStatus.SUCCESS.getValue());
                }
            }
            return ResultContext.success("操作成功");
        }
    }


    @Override
    public ResultContext<Void> export(BaseCrossDBOperationDTO<DataExportRecordParam, DataExportRecordDTO, ExportParam> operationReq) {
        DataExportRecordParam queryParam = operationReq.getQueryParam();
        ExportParam operationParam = operationReq.getOperationParam();
        List<String> mustNeedList = Collections.emptyList();
        boolean check = this.checkAndDealDatabaseFields(operationParam, queryParam, mustNeedList);
        if (!check) {
            throw new RuntimeException("导出字段有误，请检查！");
        }
        // 进行反选或者单选的处理
        SelectAllIncludeParam selectAllIncludeParam = operationReq.selectAllIncludeParam(DataExportRecordDTO::getUrid);
        operationReq.getQueryParam().setSelectAllIncludeParam(selectAllIncludeParam);
        if (StrUtil.isBlank(operationParam.getFtpFileName())) {
            operationParam.setFtpFileName("导出记录导出");
        }
        return this.commonExport(queryParam, operationParam, ENExportDeal.DATA_EXPORT);
    }

    /**
     * @param q              查询参数
     * @param exportConsumer 导出consumer
     * @param dataTranslator 翻译器
     * @param <R>            结果类型
     * @param <Q>            查询参数类型
     */
    private <R, Q extends BaseQueryParams & ExportExtraParam> void doExportQueryWithStream(
            Q q, BiConsumer<Q, ResultHandler<R>> exportConsumer, DataTranslator<R> dataTranslator, Consumer<R> dealData) {
        boolean needMask = dataMaskingService.getSwitch();
        HashSet<Integer> referenceCounter = new HashSet<>();
        HashMap<Class<?>, List<Field>> objectFieldCache = new HashMap<>();
        try {
            dataTranslator.initCache();
            exportConsumer.accept(q, resultHandler -> {
                R resultObject = resultHandler.getResultObject();
                if (resultObject != null) {
                    dataTranslator.translate(resultObject);
                    if (needMask) {
                        dataMaskingService.doMaskWithOneCycle(resultObject, referenceCounter, objectFieldCache);
                    }
                    dealData.accept(resultObject);
                }
            });
        } finally {
            dataTranslator.cleanCache();
        }
    }


    public PageInfo<DataExportRecordDTO> exportSelective(DataExportRecordParam param) {
        List<DataExportRecordDTO> resultList = dataExportRecordManager.exportSelective(param);
        this.translate(resultList);
        return new PageInfo<>(resultList);
    }


    @Override
    public long countForExport(DataExportRecordParam queryParam) {
        // 根据检索条件统计数据量
        return dataExportRecordManager.countQuery(queryParam);
    }

    @Override
    public void dataExport(String queryParamJson, Consumer<DataExportRecordDTO> dealOneDataHandler) {
        DataExportRecordParam exportParam = JSON.parseObject(queryParamJson, DataExportRecordParam.class);
        this.doExportQueryWithPage(
                exportParam,
                this::exportSelective,
                dealOneDataHandler
        );
    }

    @Override
    public <R, Q extends BaseQueryParams & ExportExtraParam> void doExportQueryWithPage(
            Q q, Function<Q, PageInfo<R>> queryFunction, Consumer<R> dealData) {

        // 如果有分库列表，需要按分库列表循环做
        if (CollectionUtil.isNotEmpty(q.getSchemaTenantMapList())) {
            for (SchemaTenantMapDTO schemaTenantMapDTO : q.getSchemaTenantMapList()) {
                q.setSchemaTenantMap(schemaTenantMapDTO);
                this.doExportQueryWithDeterminateSchemaWithPage(q, queryFunction, dealData);
            }
        } else {
            this.doExportQueryWithDeterminateSchemaWithPage(q, queryFunction, dealData);
        }
    }

    @Override
    public <R, Q extends BaseQueryParams & ExportExtraParam> void doExportQueryWithResultHandler(
            Q q, BiConsumer<Q, ResultHandler<R>> exportConsumer, DataTranslator<R> dataTranslator, Consumer<R> dealData) {
        // 如果有分库列表，需要按分库列表循环做
        if (CollectionUtil.isNotEmpty(q.getSchemaTenantMapList())) {
            for (SchemaTenantMapDTO schemaTenantMapDTO : q.getSchemaTenantMapList()) {
                q.setSchemaTenantMap(schemaTenantMapDTO);
                this.doExportQueryWithStream(q, exportConsumer, dataTranslator, dealData);
            }
        } else {
            this.doExportQueryWithStream(q, exportConsumer, dataTranslator, dealData);
        }

    }

    private <R, Q extends BaseQueryParams & ExportExtraParam> void doExportQueryWithDeterminateSchemaWithPage(
            Q q, Function<Q, PageInfo<R>> queryFunction, Consumer<R> dealData) {

        q.setPageSize(10000);
        q.setPageNum(0);
        boolean hasNext = true;
        while (hasNext) {
            q.setPageNum(q.getPageNum() + 1);
            PageInfo<R> pageInfo = queryFunction.apply(q);
            List<R> list = pageInfo.getList();
            if (!list.isEmpty()) {
                // 将每条记录做写入文件操作
                list.forEach(dealData);
            }
            hasNext = list.size() == 10000;
        }
    }

    private DataExportWriter getDataExportWriter(String dirName, String format, String taskId, String fileNamePrefix, List<String> columnTitles) {
        if (Objects.equals("CSV", format.toUpperCase())) {
            return new CSVDataExportWriter(dirName, fileNamePrefix, columnTitles, this.getMaxLengthPerFile());
        } else if (Objects.equals("EXCEL", format.toUpperCase())) {
            return new ExcelDataExportWriter(dirName, fileNamePrefix, columnTitles, this.getMaxLengthPerFile());
        } else {
            // 抛出异常
            String message = String.format("任务[%s],执行异常无效的导出格式[%s]", taskId, format);
            throw new RuntimeException(message);
        }
    }
}
