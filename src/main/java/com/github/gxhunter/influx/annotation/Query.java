package com.github.gxhunter.influx.annotation;

import java.lang.annotation.*;

/**
 * @author hunter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.METHOD)
public @interface Query{
    /**
     * 类sql语句，支持spel表达式
     * @return
     */
    String value();

    /**
     * 是否需要post请求，false时为get请求
     * @return
     */
    boolean requiresPost() default false;
}
