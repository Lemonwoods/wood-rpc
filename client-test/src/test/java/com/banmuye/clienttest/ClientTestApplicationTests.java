package com.banmuye.clienttest;

import com.banmuye.clienttest.service.TestHello;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ClientTestApplicationTests {
    @Autowired
    TestHello testHello;

    @Test
    void contextLoads() {
        String hello_from_client = testHello.sayHello("hello from client");
        System.out.println(hello_from_client);
    }

}
