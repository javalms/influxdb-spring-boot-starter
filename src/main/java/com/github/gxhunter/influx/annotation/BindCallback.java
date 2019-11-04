package com.github.gxhunter.influx.annotation;

import com.github.gxhunter.influx.Callback;

import java.lang.annotation.*;

/**
 * 只能加在{@link Callback}上
 * @author hunter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.PARAMETER)
public @interface BindCallback{
}
