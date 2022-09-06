package com.banmuye.woodrpcframework.compress;

import com.banmuye.woodrpccommon.extension.SPI;

@SPI
public interface Compress {
    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
