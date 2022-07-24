package com.banmuye.woodrpcframework.registry.zk.impl;

import com.banmuye.woodrpccommon.enums.RpcErrorMessageEnum;
import com.banmuye.woodrpccommon.exception.RpcException;
import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpccommon.extension.SPI;
import com.banmuye.woodrpccommon.utils.CollectionUtil;
import com.banmuye.woodrpcframework.loadbalance.LoadBalance;
import com.banmuye.woodrpcframework.registry.ServiceDiscovery;
import com.banmuye.woodrpcframework.registry.zk.utils.CuratorUtils;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
@Service
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl(){
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(CollectionUtil.isEmpty(serviceUrlList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CANNOT_BE_FOUND, rpcServiceName);
        }

        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("成功找到服务地址, 地址为:[{}]", targetServiceUrl.toString());
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
