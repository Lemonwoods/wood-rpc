package com.banmuye.clienttest;

import com.banmuye.clienttest.service.TestHello;
import com.banmuye.woodrpcframework.remoting.transport.netty.client.ChannelProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.banmuye"})
public class ClientTestApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ClientTestApplication.class, args);

        ChannelProvider channelProvider = (ChannelProvider) run.getBean("channelProvider");

        TestHello testHello = (TestHello) run.getBean("testHello");
        String hello_from_client = testHello.sayHello("hello from client");
        System.out.println(hello_from_client);
    }

}
