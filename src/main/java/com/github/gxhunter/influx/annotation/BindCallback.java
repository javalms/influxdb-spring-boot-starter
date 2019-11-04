package com.github.gxhunter.influx.annotation;

import java.lang.annotation.*;

/**
 * 只能加在{@link com.xm4399.influxdb.Callback}上
 * @author hunter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.PARAMETER)
public @interface BindCallback{
}
