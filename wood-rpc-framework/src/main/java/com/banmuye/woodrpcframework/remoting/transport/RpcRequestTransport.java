package com.banmuye.woodrpcframework.remoting.transport;

import com.banmuye.woodrpccommon.extension.SPI;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
