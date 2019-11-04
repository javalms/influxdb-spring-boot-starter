package com.github.gxhunter.influx;

import lombok.Data;
import org.influxdb.annotation.Column;

import java.time.Instant;

@Data
public abstract class BaseInfluxEntity{
    /**
     * 时间戳
     */
    @Column(name = "time")
    private Instant time;

}
