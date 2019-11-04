package com.github.gxhunter.influx;

import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Callback{
    public BiConsumer<InfluxDB.Cancellable, QueryResult> getOnNext(){
        return null;
    }

    public Runnable getOnComplete(){
        return null;
    }

    public Consumer<Throwable> getOnFailure(){
        return null;
    }

    /**
     * 默认10条
     * @return 分片大小
     */
    public Integer getChunkSize(){
        return 10;
    }
}
