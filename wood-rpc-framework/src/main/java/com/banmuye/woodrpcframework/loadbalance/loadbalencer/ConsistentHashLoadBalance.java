package com.banmuye.woodrpcframework.loadbalance.loadbalencer;

import com.banmuye.woodrpcframework.loadbalance.AbstractLoadBalance;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;

import java.util.List;

public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        return null;
    }
}
