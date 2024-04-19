package com.test.common.exception;

public enum BizCodeEnum {
    VALIDATE_EXCEPTION(10001,"参数格式校验失败"),
    UNKNOWN_EXCEPTION(10000,"未知异常，请联系管理员"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");

    private int code;
    private String message;

    private BizCodeEnum(int code,String message){
        this.code=code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
