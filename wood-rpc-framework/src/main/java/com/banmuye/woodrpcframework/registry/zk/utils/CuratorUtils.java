package com.banmuye.woodrpcframework.registry.zk.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CuratorUtils {
    private static CuratorFramework zkClient;

    @Value("${zookeeper.address}")
    private static String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    @Value("${zookeeper.root_path")
    private static String ZK_REGISTER_ROOT_PATH = "/wood-rpc";

    @Value("${zookeeper.base_sleep_time")
    private static int BASE_SLEEP_TIME = 1000;

    @Value("${zookeeper.max_retries")
    private static int MAX_RETRIES = 3;

    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private CuratorUtils(){}

    /**
     * 创建持久节点
     * @param zkClient
     * @param path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path){
        try{
            if(REGISTERED_PATH_SET.contains(path)||zkClient.checkExists().forPath(path)!=null){
                log.info("节点已经被创建过，节点路径为：[{}]", path);
            }else{
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("创建节点成功，节点路径为：[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("创建持久节点失败，失败的路径为：[{}]", path);
        }
    }

    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        // 检查缓存
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }

        List<String> childrenNodes = null;
        String servicePath = getZkServicePath(rpcServiceName);
        try{
            childrenNodes = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, childrenNodes);
            registerWatcher(zkClient, rpcServiceName);
        } catch (Exception e) {
            log.error("获取子节点列表失败，失败路径为：[{}]", servicePath);
        }
        return childrenNodes;
    }

    /**
     * 将rpcServiceName转化为Zookeeper中的路径
     * @param rpcServiceName
     * @return
     */
    public static String getZkServicePath(String rpcServiceName){
        return ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
    }

    /**
     *
     * @param zkClient
     * @param rpcServiceName
     * @throws Exception
     */
    public static void registerWatcher(CuratorFramework zkClient, String rpcServiceName) throws Exception {
        String servicePath = getZkServicePath(rpcServiceName);
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
                SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
            }
        };
        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCache.start();

    }

}
