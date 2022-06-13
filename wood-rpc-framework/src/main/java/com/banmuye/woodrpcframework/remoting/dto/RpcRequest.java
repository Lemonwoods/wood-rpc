package com.banmuye.woodrpcframework.remoting.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -2061882865912667431L;

    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;

    private String version;
    private String group;
}
