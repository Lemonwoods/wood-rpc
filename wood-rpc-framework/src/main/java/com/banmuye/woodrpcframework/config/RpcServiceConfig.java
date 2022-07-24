package com.banmuye.woodrpcframework.config;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
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

    public String getServiceName(){
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }

    // todo: getRpcServiceName 此函数后续可能更改
    public String getRpcServiceName(String serviceName){
        return serviceName+this.getGroup()+this.getVersion();
    }

    // todo: getRpcServiceName 此函数后续可能更改
    public String getRpcServiceName(){
        return this.getServiceName() + this.getGroup() + this.getVersion();
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
