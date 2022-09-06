package com.banmuye.servertest.service.impl;

import com.banmuye.helloapi.service.Hello;
import com.banmuye.woodrpcframework.annotation.RpcService;

@RpcService(group = "test1", version = "version1")
public class HelloImpl implements Hello {
    @Override
    public String hello(String word) {
        return "received: "+word;
    }
}
