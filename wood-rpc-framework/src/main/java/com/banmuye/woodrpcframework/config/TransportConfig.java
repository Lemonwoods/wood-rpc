package com.banmuye.woodrpcframework.config;

import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpcframework.remoting.transport.RpcRequestTransport;
import com.banmuye.woodrpcframework.remoting.transport.netty.client.UnprocessedRequests;
import lombok.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransportConfig {

    @Bean
    public RpcRequestTransport getRpcTransport(){
        return ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

}
