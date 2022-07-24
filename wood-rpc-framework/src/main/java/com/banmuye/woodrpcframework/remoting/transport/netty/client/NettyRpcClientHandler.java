package com.banmuye.woodrpcframework.remoting.transport.netty.client;

import com.banmuye.woodrpccommon.enums.CompressTypeEnum;
import com.banmuye.woodrpccommon.enums.SerializationTypeEnum;
import com.banmuye.woodrpccommon.factory.SingletonFactory;
import com.banmuye.woodrpcframework.remoting.constants.RpcConstants;
import com.banmuye.woodrpcframework.remoting.dto.RpcMessage;
import com.banmuye.woodrpcframework.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler(){
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            log.info("Netty client has received a msg: [{}]", msg);
            if (msg instanceof RpcMessage){
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heart beat response type: [{}]", tmp.getData());
                }else if(messageType == RpcConstants.REQUEST_TYPE){
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState idleState = ((IdleStateEvent) evt).state();
            if(idleState == IdleState.WRITER_IDLE){
                log.info("write idle happens: [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = RpcMessage.builder()
                        .codec(SerializationTypeEnum.HESSIAN.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .data(RpcConstants.PINT).build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
