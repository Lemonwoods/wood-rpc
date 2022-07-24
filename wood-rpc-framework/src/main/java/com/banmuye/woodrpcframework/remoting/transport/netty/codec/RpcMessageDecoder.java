package com.banmuye.woodrpcframework.remoting.transport.netty.codec;


import com.banmuye.woodrpccommon.enums.CompressTypeEnum;
import com.banmuye.woodrpccommon.enums.SerializationTypeEnum;
import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpcframework.compress.Compress;
import com.banmuye.woodrpcframework.remoting.constants.RpcConstants;
import com.banmuye.woodrpcframework.remoting.dto.RpcMessage;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import com.banmuye.woodrpcframework.remoting.dto.RpcResponse;
import com.banmuye.woodrpcframework.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder(){
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if(decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decoded;
            if(frame.readableBytes() >= RpcConstants.TOTAL_LENGTH){
                try{
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf frame) {
        //前置检验
        checkMagicNumber(frame);
        checkVersion(frame);

        int fullLength = frame.readInt();
        byte messageType = frame.readByte();
        byte codecType = frame.readByte();
        byte compressType = frame.readByte();
        int requestId = frame.readInt();

        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType)
                .codec(codecType)
                .compress(compressType)
                .requestId(requestId).build();

        if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PINT);
            return rpcMessage;
        }
        if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PONE);
            return rpcMessage;
        }

        int bodyLength = fullLength -RpcConstants.HEAD_LENGTH;
        if(bodyLength> 0 ){
            byte[] bs = new byte[bodyLength];
            frame.readBytes(bs);

            //decompress
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);

            bs = compress.decompress(bs);
            String codecName = SerializationTypeEnum.getName(codecType);
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if(messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest tempValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tempValue);
            }else{
                RpcResponse tempValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tempValue);
            }
        }

        return rpcMessage;
    }

    private void checkVersion(ByteBuf frame) {
        byte version = frame.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf frame) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        frame.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }


}
