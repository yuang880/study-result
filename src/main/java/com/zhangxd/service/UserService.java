package com.zhangxd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangxd.domain.User;

import java.util.List;

/**
 * @author zhangxd
 */
public interface UserService {

    List<User> list();
}
