package com.banmuye.woodrpcframework.config;

import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpcframework.provider.ServiceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceProviderConfig {
    @Bean
    public ServiceProvider getServiceProvider(){
        return ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension("zookeeper");
    }
}
