package com.banmuye.woodrpcframework.registry;

import com.banmuye.woodrpccommon.extension.SPI;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
@SPI
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
