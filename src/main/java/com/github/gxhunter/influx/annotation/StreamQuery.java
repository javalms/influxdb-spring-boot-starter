package com.github.gxhunter.influx.annotation;


import java.lang.annotation.*;

/**
 * @author hunter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.METHOD)
public @interface StreamQuery{

    /**
     * sql
     * @return
     */
    String value();

    /**
     * 是否需要post请求，false时为get请求
     * @return
     */
    boolean requiresPost() default false;

    /**
     * 此注解优先
     * 可不指定默认为10，也可由{@link Callback#getChunkSize()}指定
     * @return 分片大小
     */
    int chunkSize() default -1;
}
