package com.zhangxd.common.loginuser;

import cn.hutool.core.lang.Assert;


public class UserContext {
    private static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    public static final String USER_SESSION = "loginUser";
    public static final String TENANT_ID = "tenantId";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String LOGIN_ID = "loginId";
    public static final String ORG_ID = "orgId";
    public static final String SWITCH_ORG_ID = "switchOrgId";

    private UserContext() {
        throw new IllegalStateException("Utility class");
    }

    public static void setUserInfo(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        UserInfo userInfo = threadLocal.get();
        Assert.notNull(userInfo, "无法获取登录用户信息！");
        return userInfo;
    }

    public static boolean hasUserInfo() {
        UserInfo userInfo = threadLocal.get();
        return userInfo != null;
    }

    public static String getUserId() {
        return getUserInfo().getUserId();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
