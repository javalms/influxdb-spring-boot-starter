package com.github.gxhunter.influx.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;

/**
 * 自定义插件可直接集成额此接口，添加{@link org.springframework.stereotype.Component}即可生效
 * 相比继承{@link AbstractInfluxInterceptor} 的插件，省去了插件配置的步骤
 */
public abstract class AutoInfluxDBInterceptor extends AbstractInfluxInterceptor implements ApplicationRunner{
    @Autowired
    private ApplicationContext mContext;

    @Override
    public final void run(ApplicationArguments args) throws Exception{
        mContext.getBean(PluginSupport.class).addPlugin(this);
    }
}
