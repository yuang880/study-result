package com.zhangxd.service.impl;

import com.zhangxd.domain.User;
import com.zhangxd.mapper.UserMapper;
import com.zhangxd.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhangxd
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> list() {
        return userMapper.selectList();
    }
}
