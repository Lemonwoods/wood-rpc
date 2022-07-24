package com.banmuye.woodrpcframework.registry.zk.impl;

import com.banmuye.woodrpcframework.registry.ServiceRegistry;
import com.banmuye.woodrpcframework.registry.zk.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.getZkServicePathWithAddress(rpcServiceName, inetSocketAddress);
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
