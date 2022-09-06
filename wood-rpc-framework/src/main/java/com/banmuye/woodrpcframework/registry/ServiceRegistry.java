package com.banmuye.woodrpcframework.registry;

import com.banmuye.woodrpccommon.extension.SPI;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
@SPI
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
