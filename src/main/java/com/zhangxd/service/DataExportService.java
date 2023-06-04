package com.zhangxd.service;


import com.github.pagehelper.PageInfo;
import com.rabbitmq.client.Channel;
import com.zhangxd.common.PageView;
import com.zhangxd.common.ResultContext;
import com.zhangxd.common.translator.DataTranslator;
import com.zhangxd.domain.BaseCrossDBOperationDTO;
import com.zhangxd.domain.BaseQueryParams;
import com.zhangxd.domain.export.*;
import com.zhangxd.enums.ENExportDeal;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhangxd
 * @version 1.0 2023/6/4
 */
public interface DataExportService {
    /**
     * 异步提交导出任务，并同步返回提交结果
     *
     * @param task 导出任务
     * @return 异步请求是否成功
     */
    boolean asyncExportTaskCommitToMq(DataExportTask task);

    /**
     * 通用导出
     *
     * @param queryParam     查询参数，如果是实现了页面全选的，需要先进行全选处理
     * @param exportParam    导出参数
     * @param <Q>            继承了基础查询参数 BaseQueryParams 的查询参数
     * @param exportDealType 导出处理枚举，用于之后路由使用哪个导出方法
     * @return 返回给前端的参数
     */
    <Q extends BaseQueryParams & ExportExtraParam> ResultContext<Void> commonExport(Q queryParam, ExportParam exportParam, ENExportDeal exportDealType);

    /**
     * 分页查询导出记录
     *
     * @param dataExportRecordParam 导出记录查询参数
     * @return 分页结果
     */
    PageView<DataExportRecordDTO> listRecord(DataExportRecordParam dataExportRecordParam);

    /**
     * 获取详情
     *
     * @param recordId 详情记录Id
     * @return 导出记录对象
     */
    ResultContext<DataExportRecordDTO> getDetail(String recordId);

    /**
     * 处理 MQ 监听到的数据
     *
     * @param dataExportTask 数据导出任务
     */
    <R> void dealMessage(DataExportTask dataExportTask, Message message, Channel channel);

    /**
     * 校验并填充数据库字段
     *
     * @param exportParam  新导出参数
     * @param queryParam   查询参数
     * @param <Q>          实现了基础页面查询参数和额外的导出参数的查询
     * @param mustNeedList 必传字段，不传可能导致翻译出错等问题的字段
     * @return 校验成功
     */
    <Q extends BaseQueryParams & ExportExtraParam> boolean checkAndDealDatabaseFields(ExportParam exportParam, Q queryParam, List<String> mustNeedList);


    /**
     * 统计已经失效的记录数
     *
     * @param dataExportRecordParam 导出参数
     * @return 返回统计数量
     */
    ResultContext<Integer> countInvalidRecord(DataExportRecordParam dataExportRecordParam);

    /**
     * 可支持分页的查询已经失效的导出记录
     * 分页的目的是为了防止加载数据量过大，导致内存崩溃，查出的数据只需要包含状态,URID和存储路径，其余数据属于是无效数据，不必查出增加流量
     *
     * @param dataExportRecordParam 导出参数
     * @return 返回当前页的已失效的导出记录
     */
    ResultContext<List<DataExportRecordDTO>> listInvalidRecord(DataExportRecordParam dataExportRecordParam);

    /**
     * 删除已经过期的导出文件，默认删除的是24小时之外的
     * 如果删除失败则发出钉钉的消息预警
     *
     * @return 操作结果
     */
    ResultContext<Void> deleteExpireCacheFile(List<DataExportRecordDTO> invalidRecord);

    /**
     * 删除FTP上过期的记录
     *
     * @param invalidRecord 待从FTP上删除的记录
     * @return 成功从FTP上删除的列表
     */
    ResultContext<List<DataExportRecordDTO>> deleteExpireFTPFile(List<DataExportRecordDTO> invalidRecord);

    /**
     * 将指定的记录状态更新为过期
     *
     * @param invalidRecord 待操作数据记录
     * @return 操作结果
     */
    ResultContext<Void> expireDataExportRecord(List<DataExportRecordDTO> invalidRecord);

    ResultContext<Void> export(BaseCrossDBOperationDTO<DataExportRecordParam, DataExportRecordDTO, ExportParam> operationReq);


    /**
     * 使用分页去做导出查询
     *
     * @param q             查询参数
     * @param queryFunction 查询方法
     * @param dealData      实现了ExportDeal接口的处理数据方法
     * @param <R>           结果类型
     * @param <Q>           查询参数类型
     */
    <R, Q extends BaseQueryParams & ExportExtraParam> void doExportQueryWithPage(
            Q q,
            Function<Q, PageInfo<R>> queryFunction,
            Consumer<R> dealData
    );


    /**
     * 流式导出
     *
     * @param q              查询参数
     * @param exportConsumer 流式导出Consumer
     * @param dataTranslator 翻译器
     * @param dealData       实现了ExportDeal接口的处理数据方法
     * @param <R>            结果类型
     * @param <Q>            查询参数类型
     */
    <R, Q extends BaseQueryParams & ExportExtraParam> void doExportQueryWithResultHandler(
            Q q,
            BiConsumer<Q, ResultHandler<R>> exportConsumer,
            DataTranslator<R> dataTranslator,
            Consumer<R> dealData
    );
}
