package com.github.gxhunter.influx.annotation;

import com.github.gxhunter.influx.config.InfluxClientRegister;
import com.github.gxhunter.influx.config.InfluxDbAutoConfig;
import com.github.gxhunter.influx.config.InfluxProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author hunter
 */
@Import({InfluxProperties.class,InfluxDbAutoConfig.class,InfluxClientRegister.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface InfluxMapperScan{
    String[] basePackages() default "";
}
