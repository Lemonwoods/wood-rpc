package com.banmuye.woodrpcframework.remoting.dto;

import lombok.Data;

@Data
public class RpcMessage {
    private String requestId;

    private byte messageType;
    private byte serializationType;
    private byte compressType;

    private Object data;
}
