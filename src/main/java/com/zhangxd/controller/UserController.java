package com.zhangxd.controller;

import com.zhangxd.common.ResultContext;
import com.zhangxd.domain.BaseCrossDBOperationDTO;
import com.zhangxd.domain.User;
import com.zhangxd.domain.UserParam;
import com.zhangxd.domain.export.ExportParam;
import com.zhangxd.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author ZJBR
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/list")
    public List<User> queryAll() {
        return userService.list();
    }

    @PostMapping("/exportAll")
    public ResultContext<Void> exportAll(@RequestBody BaseCrossDBOperationDTO<UserParam, User, ExportParam> operationDTO) {
        operationDTO.setAllSelect(true);
        operationDTO.setSelectData(Collections.emptyList());
        return userService.export(operationDTO);
    }

}
