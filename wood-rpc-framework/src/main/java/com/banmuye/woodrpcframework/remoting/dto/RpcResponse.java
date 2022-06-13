package com.banmuye.woodrpcframework.remoting.dto;

import com.banmuye.woodrpccommon.enums.RpcResponseCodeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 7311105465326757537L;

    private String requestId;
    private Integer code;
    private String message;

    private T data;

    public void setCodeAndMessage(RpcResponseCodeEnum rpcResponseCodeEnum){
        this.setCode(rpcResponseCodeEnum.getCode());
        this.setMessage(rpcResponseCodeEnum.getMessage());
    }

    public static <T> RpcResponse<T> success(T data, String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCodeAndMessage(RpcResponseCodeEnum.SUCCESS);
        response.setRequestId(requestId);
        if(data!=null){
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCodeAndMessage(rpcResponseCodeEnum);
        return response;
    }
}
