package com.banmuye.woodrpccommon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RpcResponseCodeEnum {
    SUCCESS(200, "The remote call succeeds"),
    FAIL(500, "The remote call fails");

    private final int code;
    private final String message;
}
