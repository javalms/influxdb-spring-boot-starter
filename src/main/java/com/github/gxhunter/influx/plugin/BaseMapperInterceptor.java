package com.github.gxhunter.influx.plugin;

import com.github.gxhunter.influx.BaseInfluxEntity;
import com.github.gxhunter.influx.BaseInfluxMapper;
import lombok.AllArgsConstructor;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * {@link BaseInfluxMapper}内置的通用方法
 * @author hunter
 */
public class BaseMapperInterceptor extends AbstractInfluxInterceptor{

    @Override
    public boolean match(Class entityClass,Method method,Object[] args){
        return method.getDeclaringClass() == BaseInfluxMapper.class;
    }

    @Override
    public Object exec(Class entityClass,Method method,Object[] args){
        return MethodMapEnum.map(method).apply(this,entityClass,method,args);
    }


    @FunctionalInterface
    interface TargetFunction{
        Object apply(BaseMapperInterceptor baseMapperPlugin,Class entityClass,Method method,Object... args);
    }


    private Object push(Class entityClass,Method method,Object[] args){
        Point point = Point.measurementByPOJO(entityClass)
                .addFieldsFromPOJO(args[0])
                .build();
        mInfluxTemplate.write(mInfluxProperties.getDatabase(),mInfluxProperties.getRetentionPolicy(),point);
        return null;
    }

    private Object batchPush(Class entityClass,Method method,Object args){
        BatchPoints.Builder builder
                = BatchPoints.database(mInfluxProperties.getDatabase())
                .retentionPolicy(mInfluxProperties.getRetentionPolicy());

        List params = (List) args;
        for(Object param : params){
            builder.point(Point.measurementByPOJO(entityClass)
                    .addFieldsFromPOJO(param)
                    .build());
        }

        mInfluxTemplate.write(builder.build());
        mInfluxTemplate.flush();
        return null;
    }

    /**
     * @param entityClass 实体
     * @param method      方法
     * @param args        包含开始和结束时间
     * @return
     */
    private Object selectBetween(Class<?> entityClass,Method method,Object[] args){
        Instant startTime = (Instant) args[0];
        Instant endTime = (Instant) args[1];
        String sql = "select * from " + tableName(entityClass) + "where time < " + endTime.toEpochMilli() * 1000000
                + " AND " + "time>" + startTime.toEpochMilli() * 1000000;
        Query query = new Query(sql,mInfluxProperties.getDatabase());
        QueryResult result = mInfluxTemplate.query(query);
        return mResultMapper.toPOJO(result,entityClass);
    }

    private Object selectBefore(Class<?> entityClass,Method method,Object[] args){
        Instant instant = (Instant) args[0];
        String sql = "select * from " + tableName(entityClass) + " where time < " + instant.toEpochMilli() * 1000000;
        Query query = new Query(sql,mInfluxProperties.getDatabase());
        QueryResult result = mInfluxTemplate.query(query);
        return mResultMapper.toPOJO(result,entityClass);
    }

    private Object selectAfter(Class<?> entityClass,Method method,Object[] args){
        Instant instant = (Instant) args[0];
        String sql = "select * from " + tableName(entityClass) + " where time>" + instant.toEpochMilli() * 1000000;
        Query query = new Query(sql,mInfluxProperties.getDatabase());
        QueryResult result = mInfluxTemplate.query(query);
        return mResultMapper.toPOJO(result,entityClass);
    }

    private Object selectCallback(Class<?> entityClass,Method method,Object[] args){
        Instant startTime = (Instant) args[0];
        Instant endTime = (Instant) args[1];
        Consumer<QueryResult> onSuccess = (Consumer<QueryResult>) args[2];
        Consumer<Throwable> onFailure = (Consumer<Throwable>) args[3];
        String sql = "select * from " + tableName(entityClass) + "where time < " + endTime.toEpochMilli() * 1000000
                + " AND " + "time>" + startTime.toEpochMilli() * 1000000;
        Query query = new Query(sql,mInfluxProperties.getDatabase());
        mInfluxTemplate.query(query,onSuccess,onFailure);
        return null;
    }

    private Object selectStream(Class<?> entityClass,Method method,Object[] args){
        String sql = (String) args[0];
        Integer chunkSize = (Integer) args[1];
        BiConsumer<InfluxDB.Cancellable, QueryResult> onNext = (BiConsumer<InfluxDB.Cancellable, QueryResult>) args[2];
        Runnable onComplete = (Runnable) args[3];
        Consumer<Throwable> onFailure = (Consumer<Throwable>) args[4];
        Query query = new Query(sql,mInfluxProperties.getDatabase());
        mInfluxTemplate.query(query,chunkSize,onNext,onComplete,onFailure);
        return null;
    }

    /**
     * 内置方法与处理器方法 映射
     */
    @AllArgsConstructor
    public enum MethodMapEnum{
        PUSH(BaseMapperInterceptor::push,"push",BaseInfluxEntity.class),
        BATCH_PUSH(BaseMapperInterceptor::batchPush,"batchPush",List.class),
        SELECT_BETWEEN(BaseMapperInterceptor::selectBetween,"selectBetween",Instant.class,Instant.class),
        SELECT_AFTER(BaseMapperInterceptor::selectAfter,"selectAfter",Instant.class),
        /**
         * 回调查询
         */
        SELECT_CALLBACK(BaseMapperInterceptor::selectCallback,"selectCallback",Instant.class,Instant.class,Consumer.class,Consumer.class),
        /**
         * 流式查询
         */
        SELECT_STREAM(BaseMapperInterceptor::selectStream,"selectStream",String.class,Integer.class,BiConsumer.class,Runnable.class,Consumer.class),
        SELECT_BEFORE(BaseMapperInterceptor::selectBefore,"selectBefore",Instant.class);
        /**
         * 接口方法
         */
        private Method interfaceMethod;
        /**
         * 目标方法
         */
        private TargetFunction mTargetFunction;

        /**
         * @param targetFunction  对应的方法映射
         * @param methodName      接口方法名
         * @param methodParamType 接口参数列表（避免重载的混淆）
         */
        MethodMapEnum(TargetFunction targetFunction,String methodName,Class<?>... methodParamType){
            try{
                this.interfaceMethod = BaseInfluxMapper.class.getMethod(methodName,methodParamType);
                this.mTargetFunction = targetFunction;
            }catch(NoSuchMethodException e){
                e.printStackTrace();
            }
        }

        /**
         * 通过接口方法 查找映射实现
         *
         * @param method 接口方法
         * @return 映射到的目标方法
         * @throws IllegalArgumentException 找不到映射关系时抛出异常
         */
        static TargetFunction map(Method method){
            for(MethodMapEnum value : MethodMapEnum.values()){
                if(value.interfaceMethod.equals(method)){
                    return value.mTargetFunction;
                }
            }
            throw new IllegalArgumentException("未找到该签名对应的处理映射：" + method.getName());
        }

    }
}
