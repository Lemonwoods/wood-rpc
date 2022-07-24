package com.banmuye.woodrpcframework.provider;

import com.banmuye.woodrpcframework.config.RpcServiceConfig;

public interface ServiceProvider {
    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);
}

