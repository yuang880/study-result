package com.zhangxd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangxd.domain.User;
import com.zhangxd.domain.UserParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

/**
* @author ZJBR
* @description 针对表【sys_user(用户信息表)】的数据库操作Mapper
* @createDate 2023-05-30 19:06:37
* @Entity com.zhangxd.domain.User
*/
@Mapper
public interface UserMapper {


    List<User> selectList();

    void exportSelective(UserParam param, ResultHandler<User> handler);

//    long count(UserParam queryParam);
}
