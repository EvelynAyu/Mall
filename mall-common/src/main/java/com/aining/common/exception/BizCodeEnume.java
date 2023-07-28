package com.aining.common.exception;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/7/25 02:51
 */

// 统一异常状态码
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败");

    private int code;
    private String msg;
    BizCodeEnume(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

