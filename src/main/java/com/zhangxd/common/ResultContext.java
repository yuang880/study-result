package com.zhangxd.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zhangxd.enums.ENMsgCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author zhangxd
 * @version 1.0 2023/6/4
 */
@Getter
@Setter
@NoArgsConstructor
@ApiModel("响应参数")
public class ResultContext<T> implements Serializable {

    private static final long serialVersionUID = 5856432252782588227L;

    /**
     * 状态码，对应枚举ENMsgCode
     * 0：请求操作成功
     * 1：业务错误
     * 2：系统异常
     * 3：未登录
     * 4：session超时
     * 5：没有权限
     */
    @ApiModelProperty(value = "状态码 0：请求操作成功, 1：业务错误, 2：系统异常, 3：未登录, 4：session超时, 5：没有权限")
    private String code;

    /**
     * 提示信息
     */
    @ApiModelProperty(value = "提示信息")
    private String info;

    /**
     * 用于区分错误细节的值，比如登录失败的多种情况
     */
    @ApiModelProperty(value = "错误细节的值")
    private String value;

    /**
     * 成功标识
     */
    @ApiModelProperty(value = "成功标识")
    private Boolean success;

    /**
     * 需要返回的数据
     */
    @ApiModelProperty(value = "返回的数据")
    private T data;
    /**
     * 对于列表操作，执行成功的记录数量
     */
    @ApiModelProperty(value = "成功记录数")
    private long successNum;

    public ResultContext(T data) {
        this();
        setData(data);
    }

    private ResultContext(String code, String info, String value, Boolean success) {
        this.code = code;
        this.info = info;
        this.value = value;
        this.success = success;
        this.successNum = 0;
    }

    private ResultContext(String code, String info, Boolean success) {
        this.code = code;
        this.info = info;
        this.success = success;
        this.successNum = 0;
    }

    public ResultContext(String code, String info, Boolean success, T data) {
        this.code = code;
        this.info = info;
        this.success = success;
        this.data = data;
        this.successNum = 0;
    }

    public ResultContext(String code, String info, String value, Boolean success, T data) {
        this.code = code;
        this.info = info;
        this.value = value;
        this.success = success;
        this.data = data;
    }

    public static <T> ResultContext<T> success(String info) {
        return new ResultContext<>(ENMsgCode.SUCCESS.getValue(), info, true);
    }

    public static <T> ResultContext<T> businessFail(String info, String value) {
        return new ResultContext<>(ENMsgCode.BUSINESS_ERROR.getValue(), info, value, false);
    }

    public static <T> ResultContext<T> businessFail(String info, String value,T data) {
        return new ResultContext<>(ENMsgCode.BUSINESS_ERROR.getValue(), info, value, false,data);
    }


    public static ResultContext systemException(String info, String value) {
        return new ResultContext(ENMsgCode.SYSTEM_EXCEPTION.getValue(), info, value, false);
    }


    public static <T> ResultContext<T> buildSuccess(String info, T data) {
        return new ResultContext<>(ENMsgCode.SUCCESS.getValue(), info, true, data);
    }

    /**
     * 对于批量操作数据的接口，需要能够提示操作成功多少条以及失败多少条记录
     * 因此需要设置成功操作数量
     *
     * @param info       提示信息
     * @param successNum 成功操作数据
     * @param <T>        数据类型
     * @return 提示操作结果
     */
    public static <T> ResultContext<T> buildSuccessNum(String info, long successNum) {
        ResultContext<T> resultContext = new ResultContext<>(ENMsgCode.SUCCESS.getValue(), info, true, null);
        resultContext.setSuccessNum(successNum);
        return resultContext;
    }

    /**
     * 对于批量操作数据的接口，需要能够提示操作成功多少条以及失败多少条记录
     * 因此需要设置成功操作数量
     *
     * @param successNum 成功操作数据
     * @param <T> 数据类型
     * @return 提示操作结果
     */
    public static <T> ResultContext<T> buildSuccessNum(long successNum) {
        ResultContext<T> resultContext = new ResultContext<>(ENMsgCode.SUCCESS.getValue(), null, true, null);
        resultContext.setSuccessNum(successNum);
        return resultContext;
    }


    /**
     * 对于批量操作数据的接口，需要能够提示操作成功多少条以及失败多少条记录
     * 因此需要设置成功操作数量,本方法固定返回成功1条
     *
     * @param info 提示信息
     * @param <T>  数据类型
     * @return 提示操作结果
     */
    public static <T> ResultContext<T> buildSuccessOne(String info) {
        return buildSuccessNum(info, 1);
    }

    @JsonIgnore
    public T getDataWithSuccess() {
        if (!success) {
            throw new RuntimeException(info);
        }
        return data;
    }

    private void copyFromAnotherContextWithoutData(ResultContext<?> resultContext) {
        this.code = resultContext.getCode();
        this.info = resultContext.getInfo();
        this.value = resultContext.getValue();
        this.success = resultContext.getSuccess();
        this.successNum = resultContext.getSuccessNum();
    }

    /**
     * 从另一个类型的ResultContext中拷贝数据
     * @param transferFunc 转换函数
     * @return 转换后的ResultContext
     * @param <F> 转换后的数据类型
     */
    public <F> ResultContext<F> copyFromAnotherType(Function<T, F> transferFunc) {
        ResultContext<F> result = new ResultContext<>();
        result.copyFromAnotherContextWithoutData(this);
        if (this.getData() != null) {
            result.data = transferFunc.apply(this.getData());
        }
        return result;
    }
}
