package com.banmuye.woodrpcframework.remoting.transport.netty.server;

import com.banmuye.woodrpccommon.enums.CompressTypeEnum;
import com.banmuye.woodrpccommon.enums.RpcResponseCodeEnum;
import com.banmuye.woodrpccommon.enums.SerializationTypeEnum;
import com.banmuye.woodrpcframework.remoting.constants.RpcConstants;
import com.banmuye.woodrpcframework.remoting.dto.RpcMessage;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import com.banmuye.woodrpcframework.remoting.dto.RpcResponse;
import com.banmuye.woodrpcframework.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ChannelHandler.Sharable
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler(){
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            if(msg instanceof RpcMessage){
                log.info("Netty server received msg: [{}]", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = RpcMessage.builder().build();
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONE);
                }else{
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage)msg).getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if(ctx.channel().isActive()&&ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    }else{
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("channel is not writable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState idleState = ((IdleStateEvent) evt).state();
            if(idleState == IdleState.READER_IDLE){
                log.info("idle happens, close the connection");
                ctx.close();
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
