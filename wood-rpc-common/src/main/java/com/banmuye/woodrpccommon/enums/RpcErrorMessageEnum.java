package com.banmuye.woodrpccommon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcErrorMessageEnum {

    CLIENT_CONNECT_SERVER_FAILURE("客户端链接服务端失败"),

    SERVICE_INVOCATION_FAILURE("服务调用失败"),

    SERVICE_CANNOT_BE_FOUND("没有找到指定服务"),

    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务没有实现接口"),

    REQUEST_NOT_MATCH_RESPONSE("返回结果错误!请求和返回不匹配");

    private final String message;
}
