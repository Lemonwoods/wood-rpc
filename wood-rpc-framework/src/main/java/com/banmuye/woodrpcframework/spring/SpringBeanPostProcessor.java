package com.banmuye.woodrpcframework.spring;

import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpcframework.annotation.RpcReference;
import com.banmuye.woodrpcframework.annotation.RpcService;
import com.banmuye.woodrpcframework.config.RpcServiceConfig;
import com.banmuye.woodrpcframework.provider.ServiceProvider;
import com.banmuye.woodrpcframework.proxy.RpcClientProxy;
import com.banmuye.woodrpcframework.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;

    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor(){
        this.serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension("zookeeper");
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for(Field declaredField:declaredFields){
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if(rpcReference!=null){
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try{
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    log.error(Arrays.toString(e.getStackTrace()));
                    log.error(e.getMessage());
                }
            }
        }
        return bean;
    }
}
