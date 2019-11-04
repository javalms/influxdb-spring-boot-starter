package com.github.gxhunter.influx.plugin;

import com.github.gxhunter.influx.BaseInfluxMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 插件支持
 * @author hunter
 */
@Slf4j
public class PluginSupport{
    private final List<AbstractInfluxInterceptor> mInterceptors = new ArrayList<>();

    /**
     * 添加插件
     *
     * @param interceptor
     */
    public PluginSupport addPlugin(AbstractInfluxInterceptor interceptor){
        mInterceptors.add(interceptor);
        return this;
    }

    /**
     * 获取插件
     *
     * @return
     */
    public List<AbstractInfluxInterceptor> getInterceptors(){
        return mInterceptors;
    }

    /**
     * 插件执行逻辑
     *
     * @param entityClass {@link BaseInfluxMapper}上的泛型
     * @param method      拦截到的方法
     * @param args        方法参数
     * @return 方法返回值
     */
    public Object invoke(Class entityClass,Method method,Object[] args){
        for(AbstractInfluxInterceptor interceptor : mInterceptors){
            if(interceptor.match(entityClass,method,args)){
                log.debug(method.toGenericString() + "已被拦截，代理插件为：" + interceptor.getClass().getName());
                return interceptor.exec(entityClass,method,args);
            }
        }
        throw new IllegalStateException("方法未找到对应实现，请自定义插件或添加合适注解：" + method.getName());
    }
}
