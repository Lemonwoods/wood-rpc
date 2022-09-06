package com.banmuye.woodrpcframework.remoting.handler;

import com.banmuye.woodrpccommon.exception.RpcException;
import com.banmuye.woodrpcframework.provider.ServiceProvider;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Component
public class RpcRequestHandler {

    @Autowired
    private ServiceProvider serviceProvider;

    public RpcRequestHandler(){

    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    public Object invokeTargetMethod(RpcRequest rpcRequest, Object service){
        Object result;
        try{
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch(NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
