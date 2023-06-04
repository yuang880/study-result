package com.zhangxd.enums;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;

public enum ENCachePrefix implements LabelAndValue<String> {

    /**
     * 默认，除非特殊情况
     * 否则不应该使用这个枚举
     */
    DEFAULT("DEFAULT#", "默认", "未使用"),

    /**
     * 菜单
     */
    MENU("MENU#", "菜单", "ID"),

    /**
     * 子功能
     */
    MENU_OPERATION("MENU_OPERATION#", "菜单子功能", "ID"),

    /**
     * 省份
     */
    PROVINCE("PROVINCE#", "省份", "无"),

    /**
     * 市
     */
    CITY("CITY#", "市", "省份ID"),

    /**
     * 区/县
     */
    COUNTY("COUNTY#", "区/县", "城市ID"),

    /**
     * 开户行信息
     */
    BANK_LOCATION("BANK_LOCATION#", "开户行", "联行号"),

    /**
     * 用户权限
     */
    RIGHT_USER("RIGHT_USER#", "用户权限", "用戶ID"),
    /**
     * 角色权限
     */
    RIGHT_ROLE("RIGHT_ROLE#", "角色权限", "角色ID"),
    /**
     * 角色
     */
    ROLE("ROLE#", "角色", "ID"),
    /**
     * 用户
     */
    USER("USER#", "用户", "ID"),

    /**
     * 商户
     */
    TENANT("TENANT#", "商户", "ID"),

    /**
     * 用户组
     */
    GROUP("GROUP#", "用户组", "ID"),

    /**
     * 组织
     */
    ORGANIZATION("ORGANIZATION#", "组织", "ID"),

    /**
     * 联系人
     */
    CONTACT("CONTACT#", "联系人", "暂未使用"),

    DICTIONARY("DICTIONARY#", "字典", "暂未使用"),

    /**
     * 账号
     */
    ACCOUNT("ACCOUNT#", "账号", "暂未使用"),

    /**
     * 渠道
     */
    CHANNEL("CHANNEL#", "渠道", "ID"),
    /**
     * 消息模板
     */
    MESSAGE_TEMPLATE("MESSAGE_TEMPLATE#", "消息模板", "ID"),
    /**
     * 消息模板字段
     */
    MESSAGE_TEMPLATE_FIELD("MESSAGE_TEMPLATE_FIELD#", "消息模板字段", "ID"),
    /**
     * 消息模板配置字段
     */
    T_MESSAGE_TEMPLATE_CONFIG_FIELD("T_MESSAGE_TEMPLATE_CONFIG_FIELD#", "消息模板配置字段", "模板Id_商户号"),

    /**
     * 消息服务器
     */
    MESSAGE_SERVER("MESSAGE_SERVER#", "消息服务器", "ID"),
    /**
     * 消息接收人
     */
    MESSAGE_RECEIVER("MESSAGE_RECEIVER#", "消息接收人", "ID"),
    /**
     * 消息
     */
    MESSAGE_MAPPING("MESSAGE_MAPPING#", "消息", "ID"),

    /**
     * 渠道字符列表
     */
    CHANNEL_CHAR_LIST("CHANNEL_CHAR_LIST#", "渠道字符列表", "渠道ID"),
    /**
     * 银行
     */
    BANK("BANK#", "银行", "ID"),
    /**
     * 消息类型
     */
    MESSAGE_TYPE("MESSAGE_TYPE#", "消息类型", "ID"),
    /**
     * 交易类型
     */
    TRANS_TYPE("TRANS_TYPE#", "交易类型", "ID"),
    /**
     * 超长字段配置
     */
    OVER_LENGTH_CONFIG("OVER_LENGTH_CONFIG#", "超长字段配置", "ID"),
    /**
     * 保融超长字段配置
     */
    FG_OVER_LENGTH_CONFIG("FG_OVER_LENGTH_CONFIG#", "保融超长字段配置", "ID"),
    /**
     * 超长字段
     */
    OVER_LENGTH_FIELD("OVER_LENGTH_FIELD#", "超长字段", "ID"),
    /**
     * 保融超长字段
     */
    FG_OVER_LENGTH_FIELD("FG_OVER_LENGTH_FIELD#", "保融超长字段", "ID"),
    /**
     * 产品映射
     */
    PRODUCT_SECTION("PRODUCT_SECTION#", "产品映射", "ID"),
    /**
     * 机构区域码映射
     */
    ORGANIZATION_AREA("ORGANIZATION_AREA#", "机构区域码映射", "ID"),
    /**
     * 渠道产品
     */
    CHANNEL_PRODUCT("CHANNEL_PRODUCT#", "渠道产品", "ID"),
    /**
     * 查询方案
     */
    QUERY_SCHEME("QUERY_SCHEME#", "查询方案", "ID"),
    /**
     * 前端配置：包括字段默认配置、字段用户配置
     */
    FRONT_CONFIG("FRONT_CONFIG#", "前端配置", ""),
    /**
     * 批转单规则
     */
    BATCH_TO_SINGLE("BATCH_TO_SINGLE#", "批转单规则", "ID"),
    /**
     * 渠道返回码映射
     */
    CHANNEL_RESP_CODE_MAPPING("CHANNEL_RESP_CODE_MAPPING#", "渠道返回码映射", "ID"),
    /**
     * 渠道异常返回信息
     */
    EXCEPTION_INFO("EXCEPTION_INFO#", "渠道异常返回信息", "ID"),

    //交易相关缓存key前缀开始
    /**
     * FTP服务器信息
     */
    T_FTP_SERVER("T_FTP_SERVER#", "FTP服务器信息", "商户_用途类型"),

    /**
     * 渠道FTP服务器信息
     */
    T_FTP_CHANNEL("T_FTP_SERVER_CHANNEL#", "渠道FTP服务器信息", "渠道代码_FTP类型"),

    /**
     * FTP服务器信息
     */
    T_FTP_SERVER_LIST("T_FTP_SERVER_LIST#", "FTP服务器信息", "FTP用途类型"),

    /**
     * 企业方FileListenerServer配置信息
     */
    T_FLS_ENTERPRISE("T_FLS_ENTERPRISE#", "商户FileListener", "商户编号_用途"),
    /**
     * 渠道FileListenerServer 配置信息
     */
    T_FLS_CHANNEL("T_FLS_CHANNEL#", "渠道FileListener", "渠道代码_用途类型"),
    /**
     * FILE_LISTENER_SERVER 列表
     */
    T_FLS_LIST("T_FLS_LIST#", "FILE_LISTENER 服务器列表", "用途类型"),

    /**
     * 商户
     */
    T_TENANT("T_TENANT#", "交易-商户", "商户号"),
    /**
     * 联行号地区码映射
     */
    T_CITY_CODE_MAPPING("T_CITY_CODE_MAPPING#", "联行号地区码映射", "联行号地区码"),
    /**
     * 账号产品
     */
    T_ACCOUNT_PRODUCT("T_ACCOUNT_PRODUCT#", "交易-账号产品", "商户_账号_业务类型_交易类型"),
    /**
     * 渠道
     */
    T_CHANNEL("T_CHANNEL#", "交易-渠道", "渠道代码"),
    /**
     * 渠道支持的商户
     */
    T_CHANNEL_SUPPORT_ENT("T_CHANNEL_SUPPORT_ENT#", "渠道-支持的商户", "渠道_商户"),
    /**
     * 渠道支持的指令码
     */
    T_CHANNEL_COMMAND("T_CHANNEL_COMMAND#", "渠道-指令码", "渠道_指令码"),
    /**
     * 渠道产品
     */
    T_CHANNEL_PRODUCT("T_CHANNEL_PRODUCT#", "交易-渠道产品", "商户_账号_业务类型_交易类型"),
    /**
     * 渠道系统
     */
    T_CHANNEL_SYSTEM("T_CHANNEL_SYSTEM#", "交易-渠道系统", "渠道系统ID"),
    /**
     * 渠道产品
     */
    T_CHANNEL_PRODUCT_LIST("T_CHANNEL_PRODUCT_LIST#", "交易-渠道产品列表", "渠道_业务类型_交易类型"),
    /**
     * 渠道字符列表
     */
    T_CHANNEL_CHAR_LIST("T_CHANNEL_CHAR_LIST#", "交易-渠道字符列表", "渠道_商户"),
    /**
     * 渠道查询规则
     */
    T_CHANNEL_QUERY_RULE("T_CHANNEL_QUERY_RULE#", "交易-渠道查询规则", "渠道_业务类型"),
    /**
     * 渠道查询规则List
     */
    T_TENANT_QUERY_RULE_LIST("T_TENANT_QUERY_RULE_LIST#", "交易-渠道查询规则列表", "无"),
    /**
     * 账号
     */
    T_ACCOUNT("T_ACCOUNT#", "交易-账号", "商户_账号"),
    /**
     * 组织映射区域码
     */
    T_ORGANIZATION_AREA("T_ORGANIZATION_AREA#", "交易-组织映射区域码", "商户_组织代码"),
    /**
     * 密钥
     */
    T_SECRETKEY("T_SECRETKEY#", "交易-密钥", "商户"),
    /**
     * 渠道密钥
     */
    T_SECRETKEY_CHANNEL("T_SECRETKEY_CHANNEL#", "交易-渠道密钥", "渠道系统ID"),
    /**
     * RSA软证书
     */
    T_SOFTKEY("T_SOFTKEY#", "交易-RSA软证书", "商户_渠道_RSA密钥类型_密钥业务类型"),
    /**
     * 区域名称映射code
     */
    T_AREA("T_AREA#", "交易-区域名称映射", "区域名称（需要编码）"),
    /**
     * 系统配置
     */
    T_SYSTEM_PROPERTIES("T_SYSTEM_PROPERTIES#", "交易-系统配置", "配置项"),
    /**
     * 超长字段配置
     */
    T_OVER_LENGTH_CFG("T_OVER_LENGTH_CFG#", "交易-超长字段配置", "渠道_银行指令"),
    /**
     * 保融超长字段配置
     */
    T_FG_OVER_LENGTH_CONFIG("T_FG_OVER_LENGTH_CONFIG#", "交易-保融超长字段配置", "业务类型：0-签约/解约,1-单笔，2-批量,3-单笔签约扣款,4-批量签约扣款"),
    /**
     * 渠道维护白名单
     */
    T_MAINTAIN_WHITE("T_MAINTAIN_WHITE#", "交易-渠道维护白名单", "商户_渠道_渠道产品"),
    /**
     * 批转单规则
     */
    T_BATCH_TO_SINGLE_RULE("T_BATCH_TO_SINGLE_RULE#", "交易-批转单规则", "渠道产品ID_账户"),
    /**
     * 批转单规则列表
     */
    T_BATCH_TO_SINGLE_RULE_LIST("T_BATCH_TO_SINGLE_RULE_LIST#", "交易-批转单规则列表", "渠道ID"),
    /**
     * 商户编号列表
     */
    T_TENANT_CODE_LIST("T_TENANT_CODE_LIST#", "交易-商户编号列表", "无"),
    /**
     * 渠道返回码映射列表
     */
    T_CHANNEL_RESP_CODE_MAPPING_LIST("T_CHANNEL_RESP_CODE_MAPPING_LIST#", "交易-渠道返回码映射列表", "渠道代码_收付方向_业务类型"),
    /**
     * 渠道区域码列表
     */
    T_CHANNEL_AREA_CODE_LIST("T_CHANNEL_AREA_CODE_LIST#", "交易-渠道区域码列表", "渠道代码"),
    /**
     * 未知返回信息
     */
    T_UNKNOWN_INFORMATION("T_UNKNOWN_INFORMATION#", "交易-未知返回信息", "ID"),
    /**
     * 拦截返回信息
     */
    T_INTERCEPT_INFORMATION("T_INTERCEPT_INFORMATION#", "交易-拦截返回信息", "渠道系统id"),
    /**
     * 渠道异常返回信息
     */
    T_EXCEPTION_INFO("T_EXCEPTION_INFO#", "交易-渠道异常返回信息", "渠道代码_收付方向"),
    /**
     * 渠道商户签名版本
     */
    T_CHANNEL_TENANT_SIGN_VERSION("T_CHANNEL_TENANT_SIGN_VERSION#", "交易-渠道商户签名版本", "渠道代码_业务类型_商户号"),
    /**
     * 渠道签名版本签名字段
     */
    T_CHANNEL_SIGN_FIELD("T_CHANNEL_SIGN_FIELD#", "交易-渠道签名版本签名字段", "渠道代码_指令代码_签名字段接口类型_签名版本号_字段类型"),
    /**
     * 批次明细查询延时缓存
     */
    BATCH_DETAIL_DELAY("BATCH_DETAIL_DELAY#", "批次明细查询延时缓存", "商户号_批次号_批次流水号"),
    /**
     * 存量签约批次明细查询延时缓存
     */
    SIGN_BATCH_DETAIL_DELAY("SIGN_BATCH_DETAIL_DELAY#", "存量签约批次明细查询延时缓存", "商户号_批次号_协议号"),
    /**
     * 单笔查询延时缓存
     */
    SINGLE_DELAY("SINGLE_DELAY#", "单笔查询延时缓存", "商户号_交易流水"),

    /**
     * 测试用 交易时需拦截状态
     */
    T_DEAL_STATE_INTERCEPT("T_DEAL_STATE_INTERCEPT#", "测试用交易时需拦截状态", "批次处理状态"),

    /**
     * 导入
     */
    BUSINESS_IMPORT("BUSINESS_IMPORT#", "导入", "未知"),

    /**
     * 导出
     */
    BUSINESS_EXPORT("BUSINESS_EXPORT#", "导出", "暂未使用"),

    /**
     * 缓存统计
     */
    CACHE_MISSING_STATISTICS("CACHE_MISSING_STATISTICS#", "缓存统计", "未知"),

    /**
     * 单笔流水号映射
     */
    S_TRANS_NO_MAP("S_TRANS_NO_MAP#", "单笔流水号映射", "商户号_原始流水号"),

    /**
     * 单笔记录
     */
    S_SINGLE_RECORD("S_SINGLE_RECORD#", "单笔记录", "商户号_原始流水号"),

    /**
     * 单笔交易记录查表次数
     */
    S_SINGLE_CACHE_GET_FROM_DB_TIMES("S_SINGLE_CACHE_GET_FROM_DB_TIMES#", "单笔交易记录查表次数", "商户号_原始流水号"),

    /**
     * 单笔查询频率
     */
    S_QUERY_FREQUENCY("S_QUERY_FREQUENCY#", "单笔查询频率", "商户号_映射后流水号"),

    /**
     * 单笔查询报文
     */
    S_QUERY_MESSAGE("S_QUERY_MESSAGE#", "单笔查询报文", "商户号_映射后流水号"),

    /**
     * 强制返回报文缓存
     */
    S_FORCE_RETURN("S_FORCE_RETURN#", "单笔查询报文", "商户号_映射后流水号"),

    /**
     * 强制返回报文缓存
     */
    S_SIGN_FORCE_RETURN("S_SIGN_FORCE_RETURN#", "单笔签约查询报文", "商户号_原始协议号_签约解约标记"),

    /**
     * 强制返回报文缓存
     */
    S_SMS_FORCE_RETURN("S_SMS_FORCE_RETURN#", "移动扣款强制返盘报文报文", "商户号_流水号_原始流水号_指令码"),

    /**
     * 银联卡表
     */
    T_UNIONPAY_CARD("T_UNIONPAY_CARD", "匹配-银联卡列表", ""),

    /**
     * 太多未知交易计数
     */
    S_TOO_MUCH_KNOWN("S_TOO_MUCH_KNOWN#", "太多未知交易计数", "商户号"),

    /**
     * 消息预警统计结果
     */
    MSG_WARN_COUNT_TIME("MSG_WARN_COUNT_TIME#", "消息预警统计结果", "商户号_渠道_消息类型"),

    /**
     * 消息预警方案
     */
    MSG_WARN_PLAN("MSG_WARN_PLAN#", "消息预警方案", "URID"),

    /**
     * 单笔健康检测最后时间
     */
    HEALTH_CHECK_TIME("HEALTH_CHECK_TIME", "单笔健康检测最后时间", ""),

    /**
     * 批量返盘文件是否一致校验DTO
     */
    BATCH_REC_FILE_DIFF_CHECK_DTO("BATCH_REC_FILE_DIFF_CHECK_DTO#", "批量返盘文件是否一致校验DTO", "商户号_批次号"),

    /**
     * 批转单银行响应计数緩存
     */
    BTS_RETURN_COUNT_MAP_CACHE("BTS_RETURN_COUNT_MAP_CACHE", "批转单银行响应计数緩存", ""),

    BTS_QUERY_RETURN_COUNT_MAP("BTS_QUERY_RETURN_COUNT_MAP", "批转单银行查询响应计数", ""),

    SIGN_BATCH_REC_FILE_DIFF_CHECK_DTO("SIGN_BATCH_REC_FILE_DIFF_CHECK_DTO#", "存量签约返盘文件是否一致校验DTO", "商户号_批次号"),

    /**
     * 银行代码映射
     */
    T_BANK_CODE_MAPPING("T_BANK_CODE_MAPPING#", "银行代码映射表", "商户_映射前银行代码"),

    /**
     * 银行信息
     */
    T_BANK("T_BANK", "银行信息列表", ""),

    /**
     * 送盘文件下载失败列表
     */
    BATCH_DOWNLOAD_FAIL_LIST("BATCH_DOWNLOAD_FAIL_LIST#", "送盘文件下载失败列表", "商户号_交易日期"),

    /**
     * 渠道特殊字符转换列表
     */
    T_CHAR_CONVERT_LIST_WITH_CHANNEL("T_CHAR_CONVERT_LIST_WITH_CHANNEL", "渠道特殊字符转换列表", "渠道编码_交易类型"),

    /**
     * 交易类型
     */
    T_TRANS_TYPE("T_TRANS_TYPE#", "交易类型", "交易类型"),

    /**
     * 前置机跳转配置
     */
    FC_BANK_JUMP_CONFIG("FC_BANK_JUMP_CONFIG", "前置机跳转配置", "前置机跳转配置"),

    /**
     * 限流控制器
     */
    T_RATE_LIMTER("T_RATE_LIMTER#", "限流控制器", "限流控制器"),

    /**
     * 批次状态更新时间
     */
    T_BATCH_DEAL_STATE_UPD_TIME("T_BATCH_DEAL_STATE_UPD_TIME#", "批次状态及更新时间", "批次状态及更新时间"),
    /**
     * 钉钉预警机器人缓存
     */
    T_DING_WARN_ROBOT("T_DING_WARN_ROBOT", "钉钉预警机器人缓存", "钉钉预警机器人缓存"),

    /**
     * 不支持客户方银行
     */
    UN_SUPPORT_CUST_BANK("UN_SUPPORT_CUST_BANK#", "不支持客户方银行", "渠道ID，Hash的key为客户方银行编码"),
    /**
     * 渠道产品临时维护时间，使用Redisson.getMap()来获取，用getBucket()获取不到，redisson高版本才可以
     */
    T_CHANNEL_TEMP_MAINTAINTIME("T_CHANNEL_TEMP_MAINTAINTIME", "渠道产品临时维护时间", "Map结构：{channelProductId: List<CacheDTO>"),
    /**
     * 工作日缓存，用来存储某天是否为工作日，Map结构
     */
    WORK_DAY_FLAG("WORK_DAY_FLAG#","工作日缓存","工作日缓存"),

    /**
     * 退票消息模板
     */
    T_REFUND_MESSAGE_TEMPLATE("T_REFUND_MESSAGE_TEMPLATE#", "对账消息模板下拉", "模板Id"),
    /**
     * 消息模板退票字段配置
     */
    T_REFUND_FIELD("T_REFUND_FIELD#", "消息模板退票字段配置", "模板Id"),
    /**
     * 移动扣款查询延时缓存
     */
    SMS_AUTH_DELAY("SMS_AUTH_DELAY#", "移动扣款查询延时缓存", "商户号_交易流水"),
    /**
     * 移动扣款查询频率
     */
    SMS_AUTH_QUERY_FREQUENCY("SMS_AUTH_QUERY_FREQUENCY#", "移动扣款查询频率", "商户号_映射后流水号"),
    SINGLE_UPDATE_ACCOUNTING_DATE_LOCK("SINGLE_UPDATE_ACCOUNTING_DATE_LOCK#", "单笔通知回来尝试更新记账日期的锁", "商户号_流水号"),

    T_HANDLING_CUST_ACC("T_HANDLING_CUST_ACC#", "交行处理中账号列表", "RSet结构，需要设置缓存过期时间"),
    T_RECON_CHANNEL("T_RECON_CHANNEL#", "对账渠道", "渠道编码"),

    T_RECON_CHANNEL_PRODUCT("T_RECON_CHANNEL_PRODUCT#", "对账渠道产品", "渠道编码_业务类型_交易类型"),

    T_RECON_ACCOUNT_PRODUCT("T_RECON_ACCOUNT_PRODUCT#", "对账账户产品", "账号id_业务类型_交易类型"),

    FC_CONFIG_VERSION("FC_CONFIG_VERSION", "企业前置机配置版本缓存", "商户号_前置机编号_前置机版本号"),
    ;

    private String value;
    private String label;
    private String structure;

    ENCachePrefix(String value, String label, String structure) {
        this.value = value;
        this.label = label;
        this.structure = structure;
    }


    public static ENCachePrefix getEumByPrefixMatch(String value) {
        return StrUtil.isBlank(value) ? null :
            Arrays.stream(ENCachePrefix.values())
                .filter(x -> value.startsWith(x.value))
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("枚举【%s】中不存在值【%s】", ENCachePrefix.class.getSimpleName(), value)));

    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getStructure() {
        return structure;
    }

    public static boolean isMapKey(String value) {
        return T_CHANNEL_TENANT_SIGN_VERSION.value.equals(value) || T_CHANNEL_SIGN_FIELD.value.equals(value);
    }
}
