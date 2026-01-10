package com.hksc.common.result;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> success(){
        return success(null);
    }

    public static <T> Result<T> success(T data){
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<T>();
        result.setCode(500);
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg){
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }

    /**
     * 通用构建方法，可以自定义 code, message 和 data
     */
    public static <T> Result<T> build(T data, Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setData(data);
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }
}