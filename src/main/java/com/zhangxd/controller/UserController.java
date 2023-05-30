package com.zhangxd.controller;

import com.zhangxd.domain.User;
import com.zhangxd.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    public void exportAll() {

    }

}
