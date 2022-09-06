package com.banmuye.woodrpcframework.config;

import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpcframework.registry.ServiceRegistry;
import org.springframework.context.annotation.Bean;

public class ServiceRegistryConfig {
    @Bean
    public ServiceRegistry getServiceRegistry(){
        return ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zookeeper");
    }
}
