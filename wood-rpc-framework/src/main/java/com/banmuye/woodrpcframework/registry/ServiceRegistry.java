package com.banmuye.woodrpcframework.registry;

import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
