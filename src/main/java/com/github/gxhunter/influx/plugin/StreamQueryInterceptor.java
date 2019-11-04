package com.github.gxhunter.influx.plugin;

import com.github.gxhunter.influx.Callback;
import com.github.gxhunter.influx.annotation.BindCallback;
import com.github.gxhunter.influx.annotation.StreamQuery;
import org.influxdb.dto.Query;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 带有{@link StreamQuery}注解的方法拦截器
 * 用于处理流访问
 * @author hunter
 */
public class StreamQueryInterceptor extends AbstractInfluxInterceptor{
    @Override
    public boolean match(Class entityClass,Method method,Object[] args){
        return method.getAnnotation(StreamQuery.class) != null;
    }

    @Override
    public Object exec(Class entityClass,Method method,Object[] args){
        List<Callback> value = getParamValueByAnnotation(method,args,BindCallback.class,Callback.class);
        StreamQuery streamQuery = method.getAnnotation(StreamQuery.class);
        Integer chunkSize = null;
        if(CollectionUtils.isEmpty(value)){
            throw new IllegalArgumentException("stream执行，必须执行回调并添加BindCallback注解");
        }
        if(value.size() > 1){
            throw new IllegalArgumentException("只能有一个BindCallback");
        }

        Callback callback = value.get(0);

        if(streamQuery.chunkSize() != -1){
//        在QueryStream上已指定chunkSize以此为准
            chunkSize = streamQuery.chunkSize();
        }


        String command = SPEL_PASER.setContext(method,args).parse(streamQuery.value());

        Query query = new Query(command,mInfluxProperties.getDatabase(),streamQuery.requiresPost());

        mInfluxTemplate.query(query,chunkSize==null?callback.getChunkSize():chunkSize,callback.getOnNext(),callback.getOnComplete(),callback.getOnFailure());

        return null;
    }

    /**
     * @param method     方法
     * @param params     参数
     * @param clazz      注解类型
     * @param returnType 注解支持的类型
     * @return 带有目标注解的参数
     */
    private <T> List<T> getParamValueByAnnotation(Method method,Object[] params,
                                                  Class<? extends Annotation> clazz,Class<T> returnType){
        List<T> result = new ArrayList<>();
        Annotation[][] ans = method.getParameterAnnotations();
        for(int i = 0; i < ans.length; i++){
            for(int j = 0; j < ans[i].length; j++){
                if(clazz.isInstance(ans[i][j])){
                    if(!returnType.isInstance(params[i])){
                        throw new IllegalArgumentException("注解" + clazz.getName() + "只能加在" + params[i].getClass().getName());
                    }
                    result.add(returnType.cast(params[i]));
                }
            }
        }

        return result;
    }
}
