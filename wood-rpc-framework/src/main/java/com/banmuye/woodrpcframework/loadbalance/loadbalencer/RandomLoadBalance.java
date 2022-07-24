package com.banmuye.woodrpcframework.loadbalance.loadbalencer;

import com.banmuye.woodrpcframework.loadbalance.AbstractLoadBalance;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
