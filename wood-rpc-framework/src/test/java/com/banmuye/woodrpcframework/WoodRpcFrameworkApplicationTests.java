package com.banmuye.woodrpcframework;

import com.banmuye.woodrpcframework.registry.ServiceDiscovery;
import com.banmuye.woodrpcframework.registry.ServiceRegistry;
import com.banmuye.woodrpcframework.registry.zk.utils.CuratorUtils;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetSocketAddress;

@SpringBootTest
class WoodRpcFrameworkApplicationTests {

    @Autowired
    ServiceRegistry serviceRegistry;

    @Autowired
    ServiceDiscovery serviceDiscovery;

    @Test
    void contextLoads() {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName("com.helloService");
        rpcRequest.setGroup("test");
        rpcRequest.setVersion("v1.0");
        serviceRegistry.registerService(rpcRequest.getRpcServiceName(), new InetSocketAddress("localhost",8888));
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        System.out.println(inetSocketAddress.toString());
    }

}
