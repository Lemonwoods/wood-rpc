package com.banmuye.woodrpcframework.remoting.transport.netty.client;

import com.banmuye.woodrpccommon.enums.CompressTypeEnum;
import com.banmuye.woodrpccommon.enums.SerializationTypeEnum;
import com.banmuye.woodrpccommon.extension.ExtensionLoader;
import com.banmuye.woodrpccommon.factory.SingletonFactory;
import com.banmuye.woodrpcframework.registry.ServiceDiscovery;
import com.banmuye.woodrpcframework.remoting.constants.RpcConstants;
import com.banmuye.woodrpcframework.remoting.dto.RpcMessage;
import com.banmuye.woodrpcframework.remoting.dto.RpcRequest;
import com.banmuye.woodrpcframework.remoting.dto.RpcResponse;
import com.banmuye.woodrpcframework.remoting.transport.RpcRequestTransport;
import com.banmuye.woodrpcframework.remoting.transport.netty.codec.RpcMessageDecoder;
import com.banmuye.woodrpcframework.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient(){
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zookeeper");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @SneakyThrows
    public Channel doChannel(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future->{
            if(future.isSuccess()){
                log.info("The client has connected to [{}] successfully", inetSocketAddress.toString());
                channelCompletableFuture.complete(future.channel());
            }else{
                throw new IllegalStateException();
            }
        });

        return channelCompletableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> responseCompletableFuture = new CompletableFuture<>();

        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if(channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(), responseCompletableFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future->{
                if(future.isSuccess()){
                    log.info("Netty client has sent message: [{}]", rpcMessage);
                }else{
                    future.channel().close();
                    responseCompletableFuture.completeExceptionally(future.cause());
                    log.info("Netty failed to send message", future.cause());
                }
            });
        }else{
            throw new IllegalStateException();
        }
        return responseCompletableFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel == null){
            channel = doChannel(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
