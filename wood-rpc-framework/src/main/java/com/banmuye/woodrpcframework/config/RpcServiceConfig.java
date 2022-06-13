package com.banmuye.woodrpcframework.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RpcServiceConfig {
    private String version = "";
    private String group = "";

    private Object service;

    public List<String> getServiceNameList(){
        Class<?>[] interfaces = this.service.getClass().getInterfaces();
        List<String> serviceNameList = new ArrayList<>(interfaces.length);
        for(Class<?> i:interfaces){
            serviceNameList.add(i.getCanonicalName());
        }
        return serviceNameList;
    }

    public String getRpcServiceName(String serviceName){
        return serviceName+this.getGroup()+this.getVersion();
    }

    public List<String> getRpcServiceNameList(){
        List<String> serviceNameList = this.getServiceNameList();
        List<String> rpcServiceNameList = new ArrayList<>(serviceNameList.size());
        for(String name: serviceNameList){
            rpcServiceNameList.add(getRpcServiceName(name));
        }

        return rpcServiceNameList;
    }

}
