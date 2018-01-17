package com.xiaojinzi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 金全 wrj008
 * @version 1.0.0 2018/1/16.
 * @description
 */
@ConfigurationProperties(prefix = "es")
@Data
@Component
public class EsClientConfig {

    /** 链接ip .*/
    private String url_ip;

    /** 端口 .*/
    private Integer url_port;

    /** 集群命名 .*/
    private String node_cluster;

    /** 节点名称 .*/
    private String node_name;
}
