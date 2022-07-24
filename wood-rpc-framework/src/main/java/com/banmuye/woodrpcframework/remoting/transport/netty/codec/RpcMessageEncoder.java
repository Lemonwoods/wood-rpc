package com.banmuye.woodrpcframework.remoting.transport.netty.codec;

import com.banmuye.woodrpccommon.enums.CompressTypeEnum;
import com.banmuye.woodrpccommon.enums.SerializationTypeEnum;
import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpcframework.compress.Compress;
import com.banmuye.woodrpcframework.remoting.constants.RpcConstants;
import com.banmuye.woodrpcframework.remoting.dto.RpcMessage;
import com.banmuye.woodrpcframework.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        try {
            byteBuf.writeBytes(RpcConstants.MAGIC_NUMBER);
            byteBuf.writeByte(RpcConstants.VERSION);
            //预留四个字节空间,给后面写长度
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            byteBuf.writeByte(messageType);
            byteBuf.writeByte(rpcMessage.getCodec());
            byteBuf.writeByte(rpcMessage.getCompress());
            byteBuf.writeInt(ATOMIC_INTEGER.getAndIncrement());

            //构建body的长度
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;

            // 如果消息不是心跳相关的, 那么fullLength = head Length+bodyLength
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}]", codecName);

                // serialize
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());

                //compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                byteBuf.writeBytes(bodyBytes);
            }
            int writeIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            byteBuf.writeInt(fullLength);
            byteBuf.writerIndex(writeIndex);
        }catch (Exception e){
            log.error("Encode request error!", e);
        }
    }
}

