package com.banmuye.servertest;

import com.banmuye.helloapi.service.Hello;
import com.banmuye.servertest.service.impl.HelloImpl;
import com.banmuye.woodrpcframework.config.RpcServiceConfig;
import com.banmuye.woodrpcframework.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication(scanBasePackages = {"com.banmuye"})
public class ServerTestApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ServerTestApplication.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // Register service manually
        Hello hello = new HelloImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version1").service(hello).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();

    }

}
