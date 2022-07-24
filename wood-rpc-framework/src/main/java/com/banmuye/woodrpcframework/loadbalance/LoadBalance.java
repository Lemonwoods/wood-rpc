package com.banmuye.woodrpcframework.loadbalance;

import com.banmuye.woodrpccommon.extension.SPI;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest);
}
