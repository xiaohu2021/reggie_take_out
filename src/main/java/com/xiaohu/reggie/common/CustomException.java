package com.xiaohu.reggie.common;


import org.springframework.stereotype.Component;

/**
 * 自定义异常
 */
public class CustomException  extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
