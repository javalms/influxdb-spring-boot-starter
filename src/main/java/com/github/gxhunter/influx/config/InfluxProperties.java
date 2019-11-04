package com.github.gxhunter.influx.config;

import lombok.Data;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.influx")
@Data
public class InfluxProperties{
    /**
     * 数据库
     */
    private String database;

    /**
     * 保留策略
     */
    private String retentionPolicy;

    /**
     * 是否开启zip压缩
     */
    private boolean enableGzip;

    /**
     * 开启批量操作
     */
    private boolean batchProcessor;

}
