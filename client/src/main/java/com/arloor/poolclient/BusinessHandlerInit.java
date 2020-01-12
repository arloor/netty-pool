package com.arloor.poolclient;

import com.arloor.poolclient.businesshandler.BusinessReadhandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.string.StringDecoder;

public class BusinessHandlerInit {

    /**
     * 用于增加business handeler
     * 在ClientPoolBootStrap中会反射调用
     * @param channel
     */
    public static void addHandler(Channel channel){
        channel.pipeline().addLast(new StringDecoder());
        channel.pipeline().addLast(new BusinessReadhandler());
    }
}
