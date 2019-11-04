package com.github.gxhunter.influx.config;

import com.github.gxhunter.influx.plugin.BaseMapperInterceptor;
import com.github.gxhunter.influx.plugin.PluginSupport;
import com.github.gxhunter.influx.plugin.QueryAnnotationInterceptor;
import com.github.gxhunter.influx.plugin.StreamQueryInterceptor;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author hunter
 */
@Configuration
public class InfluxDbAutoConfig{

    @Bean
    @ConditionalOnMissingBean
    public InfluxDBResultMapper influxDBResultMapper(){
        return new InfluxDBResultMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public BaseMapperInterceptor baseMapperInterceptor(){
        return new BaseMapperInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryAnnotationInterceptor queryAnnotationInterceptor(){
        return new QueryAnnotationInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamQueryInterceptor streamQueryInterceptor(){
        return new StreamQueryInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginSupport pluginSupport(StreamQueryInterceptor streamQueryInterceptor,BaseMapperInterceptor baseMapperInterceptor,QueryAnnotationInterceptor queryAnnotationInterceptor){
        return new PluginSupport()
                .addPlugin(baseMapperInterceptor)
                .addPlugin(queryAnnotationInterceptor)
                .addPlugin(streamQueryInterceptor)
        ;
    }


}
