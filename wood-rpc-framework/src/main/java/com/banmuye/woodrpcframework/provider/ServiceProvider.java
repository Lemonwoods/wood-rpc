package com.banmuye.woodrpcframework.provider;

import com.banmuye.woodrpccommon.extension.SPI;
import com.banmuye.woodrpcframework.config.RpcServiceConfig;

@SPI
public interface ServiceProvider {
    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);
}

