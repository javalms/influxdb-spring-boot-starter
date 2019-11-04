package com.github.gxhunter.influx;

import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @param <T> 必须带有{@link org.influxdb.annotation.Measurement}注解的实体类
 */
public interface BaseInfluxMapper<T extends BaseInfluxEntity>{
    /**
     * 查询指定范围内数据
     *
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @param onSuccess 成功回调
     * @param onFailure 失败回调
     */
    void selectCallback(Instant startTime,Instant endTime,Consumer<QueryResult> onSuccess,Consumer<Throwable> onFailure);

    /**
     * 指定时间范围内的数据
     *
     * @return
     */
    List<T> selectBetween(Instant startTime,Instant endTime);

    /**
     * 指定时间之后的数据
     *
     * @param startTime
     * @return
     */
    List<T> selectAfter(Instant startTime);

    /**
     * 指定时间之前的数据
     *
     * @param endTime
     * @return
     */
    List<T> selectBefore(Instant endTime);

    /**
     * 上报单条数据
     *
     * @param entity
     */
    void push(T entity);

    /**
     * 批量上报
     *
     * @param entityList
     */
    void batchPush(List<T> entityList);


    /**
     * 流式查询
     *
     * @param sql        sql
     * @param chunkSize  一块中要处理的QueryResults的数量。
     * @param onNext     the consumer to invoke for each received QueryResult; with capability to discontinue a streaming query
     * @param onComplete 结束回调
     * @param onFailure  失败回调
     */
    void selectStream(
            String sql,
            Integer chunkSize,
            BiConsumer<InfluxDB.Cancellable, QueryResult> onNext,
            Runnable onComplete,
            Consumer<Throwable> onFailure);

}
