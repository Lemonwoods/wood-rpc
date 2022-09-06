package com.banmuye.woodrpcframework.provider.impl;

import com.banmuye.woodrpccommon.enums.RpcErrorMessageEnum;
import com.banmuye.woodrpccommon.exception.RpcException;
import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpcframework.config.RpcServiceConfig;
import com.banmuye.woodrpcframework.provider.ServiceProvider;
import com.banmuye.woodrpcframework.registry.ServiceRegistry;
import com.banmuye.woodrpcframework.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.cert.Extension;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    @Autowired
    private ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl(){
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            return;
        }
        // set为了避免重复注册，map用来后续查找对应service
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CANNOT_BE_FOUND);
        }

        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
