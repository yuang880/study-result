package com.zhangxd.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.zhangxd.common.ResultContext;
import com.zhangxd.domain.BaseCrossDBOperationDTO;
import com.zhangxd.domain.SelectAllIncludeParam;
import com.zhangxd.domain.User;
import com.zhangxd.domain.UserParam;
import com.zhangxd.domain.export.ExportParam;
import com.zhangxd.enums.ENExportDeal;
import com.zhangxd.mapper.UserMapper;
import com.zhangxd.service.DataExportService;
import com.zhangxd.service.ExportDeal;
import com.zhangxd.service.UserService;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author zhangxd
 */
@Service
public class UserServiceImpl implements UserService, ExportDeal<User, UserParam> {

    @Resource
    private UserMapper userMapper;
    @Resource
    private DataExportService dataExportService;
    @Resource
    private UserTranslator userTranslator;

    @Override
    public List<User> list() {
        return userMapper.selectList();
    }

    @Override
    public ResultContext<Void> export(BaseCrossDBOperationDTO<UserParam, User, ExportParam> operationReq) {
        List<String> mustNeedList = Collections.emptyList();
        UserParam queryParam = operationReq.getQueryParam();
        ExportParam operationParam = operationReq.getOperationParam();
        boolean check = dataExportService.checkAndDealDatabaseFields(operationParam, queryParam, mustNeedList);
        if (!check) {
            throw new RuntimeException("导出字段有误，请检查！");
        }
        // 进行反选或者单选的处理
        SelectAllIncludeParam selectAllIncludeParam = operationReq.selectAllIncludeParam(User::getUserId);
        operationReq.getQueryParam().setSelectAllIncludeParam(selectAllIncludeParam);
        if (StrUtil.isBlank(operationParam.getFtpFileName())) {
            operationParam.setFtpFileName("历史成功匹配记录导出");
        }
        return dataExportService.commonExport(queryParam, operationParam, ENExportDeal.USER_EXPORT);
    }

    @Override
    public long countForExport(UserParam queryParam) {
//        return userMapper.count(queryParam);
        return 1L;
    }

    @Override
    public void dataExport(String queryParamJson, Consumer<User> dealOneDataHandler) {
        UserParam queryParam = JSON.parseObject(queryParamJson, UserParam.class);
        dataExportService.doExportQueryWithResultHandler(
                queryParam,
                this::exportSelective,
                userTranslator,
                dealOneDataHandler
        );
    }

    private void exportSelective(UserParam param, ResultHandler<User> handler) {
        userMapper.exportSelective(param, handler);
    }
}
