package com.banmuye.clienttest.service;

import com.banmuye.helloapi.service.Hello;
import com.banmuye.woodrpcframework.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class TestHello {

    @RpcReference(group = "test2", version = "version1")
    Hello hello;
    public String sayHello(String word){
        return hello.hello(word);
    }
}
