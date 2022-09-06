package com.banmuye.woodrpcframework.serialize;

import com.banmuye.woodrpccommon.extension.SPI;

@SPI
public interface Serializer {
    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
