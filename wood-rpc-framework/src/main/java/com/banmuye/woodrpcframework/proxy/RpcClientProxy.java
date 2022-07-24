package com.banmuye.woodrpcframework.proxy;

import com.banmuye.woodrpccommon.enums.RpcErrorMessageEnum;
import com.banmuye.woodrpccommon.enums.RpcResponseCodeEnum;
import com.banmuye.woodrpccommon.exception.RpcException;
import com.banmuye.woodrpcframework.config.RpcServiceConfig;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import com.banmuye.woodrpcframework.remoting.dto.RpcResponse;
import com.banmuye.woodrpcframework.remoting.transport.RpcRequestTransport;
import com.banmuye.woodrpcframework.remoting.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private static final String INTERFACE_NAME = "interfaceName";

    private final RpcRequestTransport rpcRequestTransport;

    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig){
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport){
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoke method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .parameterTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion()).build();
        RpcResponse<Object> rpcResponse = null;
        //todo: 考虑返回值类型要不要优化一下
        if (rpcRequestTransport instanceof NettyRpcClient) {
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
        this.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if(rpcResponse == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }

        if(!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())){
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }

        if(rpcResponse.getCode()==null||!rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
    }
}
