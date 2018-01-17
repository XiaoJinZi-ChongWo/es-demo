package com.xiaojinzi.config;


import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 金全 wrj008
 * @version 1.0.0 2018/1/16.
 * @description es配置类
 */
@Configuration
public class EsClientBeanConfig {

    @Autowired
    private EsClientConfig esClientConfig;

    @Bean
    public TransportClient client() throws UnknownHostException{
         /** 节点设置 .*/
        InetSocketTransportAddress node = new InetSocketTransportAddress(
                InetAddress.getByName(esClientConfig.getUrl_ip()),
                esClientConfig.getUrl_port()
        );

        /** 参数设置 .*/
        Settings settings = Settings.builder().put(esClientConfig.getNode_cluster(),esClientConfig.getNode_name()).build();
        TransportClient client = new PreBuiltTransportClient(settings);
        /** 节点导入 .*/
        client.addTransportAddress(node);
        return client;
    }
}
