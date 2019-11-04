package com.github.gxhunter.influx;

import com.github.gxhunter.influx.plugin.PluginSupport;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;

/**
 * @author hunter
 */
@Setter
@Getter
public class InfluxDBFactoryBean implements FactoryBean<Object>, ApplicationContextAware{
    /**
     * mapper类型
     */
    private Class<? extends BaseInfluxMapper> type;

    /**
     * 实体类泛型,最核心
     *
     * @see org.influxdb.annotation.Measurement
     */
    private Class<?> entityClass;

    private PluginSupport mPluginSupport;

    @Override
    public Object getObject() throws Exception{
        return Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy,method,args) -> mPluginSupport.invoke(entityClass,method,args)
        );
    }

    @Override
    public Class<?> getObjectType(){
        return this.type;
    }


    public void setEntityClass(Class<?> entityClass){
        this.entityClass = entityClass;
    }

    public void setBeanClassName(String beanClassName){
        try{
            this.type = (Class<? extends BaseInfluxMapper>) Class.forName(beanClassName);
        }catch(ClassNotFoundException e){
            throw new IllegalArgumentException(e);
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException{
        this.mPluginSupport = context.getBean(PluginSupport.class);
    }
}
