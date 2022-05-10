package com.xiaohu.reggie.common;

import org.apache.ibatis.jdbc.Null;

/**
 * 基于ThreadLoacl封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {
   private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

   // 只针对当前线程，不会出现使用多个线程
   public static void setCurrentId(Long id){
       threadLocal.set(id);
   }

   public static Long getCurrentId(){
       return threadLocal.get();
   }
}
