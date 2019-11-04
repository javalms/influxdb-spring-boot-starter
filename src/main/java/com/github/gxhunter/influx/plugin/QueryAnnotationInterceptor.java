package com.github.gxhunter.influx.plugin;

import com.github.gxhunter.influx.annotation.Query;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.QueryResult;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 带有query注解时，走此逻辑
 * @see Query
 */
@Slf4j
public class QueryAnnotationInterceptor extends AbstractInfluxInterceptor{

    @Override
    public boolean match(Class entityClass,Method method,Object[] args){
        Query query = method.getAnnotation(Query.class);
        return query != null;
    }

    @Override
    public Object exec(Class entityClass,Method method,Object[] args){
        try{
//        返回类型
            Type returnType = method.getGenericReturnType();

            Class tType = Object.class;
//        返回值带有泛型，只取第一个
            if(method.getReturnType() == List.class){
                if(returnType instanceof ParameterizedType){
                    Type actualTypeArgument = ((ParameterizedType) (returnType)).getActualTypeArguments()[0];
                    tType = Class.forName(actualTypeArgument.getTypeName());
                }
            }else{
                throw new IllegalArgumentException("mapper查询的返回值必须是List类型");
            }

            Query annotation = method.getAnnotation(Query.class);
            String command = SPEL_PASER.setContext(method,args).parse(annotation.value());
            log.debug("请求sql：" + command);
            org.influxdb.dto.Query query = new org.influxdb.dto.Query(command,mInfluxProperties.getDatabase(),annotation.requiresPost());
            QueryResult result = mInfluxTemplate.query(query);
            return mResultMapper.toPOJO(result,tType);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }
}
