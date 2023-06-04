package com.zhangxd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangxd.common.ResultContext;
import com.zhangxd.domain.BaseCrossDBOperationDTO;
import com.zhangxd.domain.User;
import com.zhangxd.domain.UserParam;
import com.zhangxd.domain.export.ExportParam;

import java.util.List;

/**
 * @author zhangxd
 */
public interface UserService {

    List<User> list();

    ResultContext<Void> export(BaseCrossDBOperationDTO<UserParam, User, ExportParam> operationDTO);
}
