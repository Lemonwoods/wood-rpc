package com.banmuye.woodrpcframework.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChannelProvider {
    private final Map<String, Channel> channelMap;

    public ChannelProvider(){
        this.channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        // 判断是否有已存在的链接
        if(channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            // 判断链接是否可用
            if(channel != null && channel.isActive()){
                return channel;
            }else{
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel){
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
        log.info("Added new channel: [{}], this channel map size is: [{}]", key, channelMap.size());
    }

    public void remove(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("removed one channel: [{}], this channel map size is: [{}]", key, channelMap.size());
    }
}
