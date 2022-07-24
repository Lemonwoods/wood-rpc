package com.banmuye.woodrpcframework.config;

import com.banmuye.woodrpccommon.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import com.banmuye.woodrpcframework.registry.zk.utils.CuratorUtils;
import com.banmuye.woodrpcframework.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {
    private static final  CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll(){
        log.info("add shutdown hook for clear all operation");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try{
                InetSocketAddress inetSocketAddress =new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}
