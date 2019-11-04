package com.github.gxhunter.influx.plugin;

import com.github.gxhunter.influx.config.InfluxProperties;
import com.github.gxhunter.util.SpelPaser;
import org.influxdb.InfluxDB;
import org.influxdb.annotation.Measurement;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;

/**
 * 模板方法模式+责任链模式  拦截方法调用
 *
 * @author wanggx
 */
public abstract class AbstractInfluxInterceptor implements ApplicationContextAware{
    protected static final SpelPaser SPEL_PASER = SpelPaser.builder().regExp("#\\{[\\w.\\d]+}").build();

    protected InfluxDB mInfluxTemplate;

    protected InfluxProperties mInfluxProperties;

    protected InfluxDBResultMapper mResultMapper;

    /**
     * 通过实体类获取表名
     *
     * @param entityClass
     * @return
     */
    protected final String tableName(Class<?> entityClass){
        return entityClass.getAnnotation(Measurement.class).name();
    }

    /**
     * 插件是否需要接管此方法
     *
     * @param entityClass 实体类
     * @param method      方法
     * @param args        方法参数
     * @return 是否接管
     */
    public abstract boolean match(Class entityClass,Method method,Object[] args);

    /**
     * 插件执行逻辑
     *
     * @param entityClass {@link com.xm4399.influxdb.BaseInfluxMapper}上的泛型
     * @param method      拦截到的方法
     * @param args        方法参数
     * @return 方法返回值
     */
    public abstract Object exec(Class entityClass,Method method,Object[] args);


    @Override
    public final void setApplicationContext(ApplicationContext context) throws BeansException{
        this.mInfluxTemplate = context.getBean(InfluxDB.class);
        this.mInfluxProperties = context.getBean(InfluxProperties.class);
        this.mInfluxTemplate.setRetentionPolicy(mInfluxProperties.getRetentionPolicy());
        this.mResultMapper = context.getBean(InfluxDBResultMapper.class);
        if(!context.getBean(InfluxDB.class).isBatchEnabled()){
            context.getBean(InfluxDB.class).enableBatch();
        }
    }

}
